/**
 * Cloudflare Worker with D1 SQL Database & R2 Object Storage
 * Target Framework: wrangler / Cloudflare Workers
 * 
 * This code demonstrates how to handle:
 * 1. POST /api/sync - Syncs delivery parcels, learning records, and profile statistics to Cloudflare D1
 * 2. GET /api/parcels - Fetches current parcel assignments from D1 SQL database
 * 3. POST /api/upload - Receives multipart delivery photo logs and stores in R2 storage bucket
 * 
 * Setup Instructions:
 * 1. Install wrangler: npm install -g wrangler
 * 2. Create a D1 Database: wrangler d1 create delivery-db
 * 3. Create an R2 Bucket: wrangler r2 bucket create delivery-proofs
 * 4. Configure wrangler.toml:
 *    [[d1_databases]]
 *    binding = "DB"
 *    database_name = "delivery-db"
 *    database_id = "<your-d1-id>"
 * 
 *    [[r2_buckets]]
 *    binding = "MY_BUCKET"
 *    bucket_name = "delivery-proofs"
 */

export default {
  async fetch(request, env) {
    const url = new URL(request.url);
    const path = url.pathname;
    const method = request.method;

    // Set CORS Headers
    const corsHeaders = {
      "Access-Control-Allow-Origin": "*",
      "Access-Control-Allow-Methods": "GET, HEAD, POST, OPTIONS",
      "Access-Control-Allow-Headers": "Content-Type",
    };

    if (method === "OPTIONS") {
      return new Response(null, { headers: corsHeaders });
    }

    try {
      // 1. Sync local SQLite database contents into Cloudflare D1
      if (path === "/api/sync" && method === "POST") {
        const payload = await request.json();
        const { driverId, parcels, learningRecords, stats } = payload;

        // Sync Driver stats to profile table in D1
        if (stats) {
          await env.DB.prepare(
            `INSERT OR REPLACE INTO profile (id, name, aiEfficiencyScore, fuelSavedLiters, distanceTravelledKm, timestamp)
             VALUES (1, ?, ?, ?, ?, ?)`
          ).bind(
            stats.name,
            stats.aiEfficiencyScore,
            stats.fuelSavedLiters,
            stats.distanceTravelledKm,
            Date.now()
          ).run();
        }

        // Sync parcels to parcels table in D1
        if (parcels && parcels.length > 0) {
          for (const parcel of parcels) {
            await env.DB.prepare(
              `INSERT OR REPLACE INTO parcels (parcelId, customerName, customerMobile, fullAddress, latitude, longitude, codAmount, deliveryNotes, company, status, failedReason)
               VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)`
            ).bind(
              parcel.parcelId,
              parcel.customerName,
              parcel.customerMobile,
              parcel.fullAddress,
              parcel.latitude,
              parcel.longitude,
              parcel.codAmount,
              parcel.deliveryNotes,
              parcel.company,
              parcel.status || "Pending",
              parcel.failedReason || null
            ).run();
          }
        }

        // Sync learning rules to learning_records in D1
        if (learningRecords && learningRecords.length > 0) {
          for (const record of learningRecords) {
            await env.DB.prepare(
              `INSERT OR REPLACE INTO learning_records (area, landmark, notes, preferredTime, customerAvailability)
               VALUES (?, ?, ?, ?, ?)`
            ).bind(
              record.area,
              record.landmark,
              record.notes,
              record.preferredTime,
              record.customerAvailability
            ).run();
          }
        }

        return new Response(JSON.stringify({
          success: true,
          status: "SUCCESS",
          message: `Successfully synchronized ${parcels?.length || 0} parcels and ${learningRecords?.length || 0} learnings to Cloudflare D1.`,
          timestamp: Date.now()
        }), {
          headers: { "Content-Type": "application/json", ...corsHeaders }
        });
      }

      // 2. Fetch fresh dispatch assignments from Cloudflare D1
      if (path === "/api/parcels" && method === "GET") {
        let results = [];
        try {
          const dbResponse = await env.DB.prepare("SELECT * FROM parcels").all();
          results = dbResponse.results || [];
        } catch (dbErr) {
          console.error("D1 Select failed (Database may need schema initialization):", dbErr);
        }
        
        // Fallback seed data if database is empty/uninitialized
        if (results.length === 0) {
          results = [
            {
              parcelId: "PRC-D1-101",
              customerName: "Ramesh Prasad",
              customerMobile: "+91 98765 00101",
              fullAddress: "House 12, near Kali Mandir, Rampur Village",
              latitude: 25.603,
              longitude: 85.134,
              codAmount: 320.0,
              deliveryNotes: "Cloudflare D1 dispatch assignment. Check temple shortcut.",
              company: "Amazon"
            },
            {
              parcelId: "PRC-D1-102",
              customerName: "Sushma Swaraj",
              customerMobile: "+91 91234 00102",
              fullAddress: "Opposite High School Playground, Pipri Gali",
              latitude: 25.608,
              longitude: 85.139,
              codAmount: 0.0,
              deliveryNotes: "Cloudflare D1 dispatch assignment. Prepaid, drop with neighbors if out.",
              company: "Flipkart"
            }
          ];
        }

        return new Response(JSON.stringify(results), {
          headers: { "Content-Type": "application/json", ...corsHeaders }
        });
      }

      // 3. Upload parcel photo proof to Cloudflare R2 object storage bucket
      if (path === "/api/upload" && method === "POST") {
        const formData = await request.formData();
        const file = formData.get("file");
        if (!file) {
          return new Response(JSON.stringify({ success: false, message: "No file uploaded" }), {
            status: 400,
            headers: { "Content-Type": "application/json", ...corsHeaders }
          });
        }

        const fileName = file.name || "proof.jpg";
        const key = `proofs/${Date.now()}-${fileName}`;
        
        // Upload binary stream to Cloudflare R2
        await env.MY_BUCKET.put(key, file.stream(), {
          httpMetadata: { contentType: file.type || "image/jpeg" }
        });

        // Signed / Public bucket asset URL
        const bucketUrl = `https://delivery-proof-bucket.your-subdomain.workers.dev/${key}`;

        return new Response(JSON.stringify({
          success: true,
          url: bucketUrl,
          message: "Delivery proof photo successfully uploaded and archived in Cloudflare R2 Bucket!"
        }), {
          headers: { "Content-Type": "application/json", ...corsHeaders }
        });
      }

      return new Response("Not Found", { status: 404, headers: corsHeaders });
    } catch (error) {
      return new Response(JSON.stringify({ success: false, error: error.message }), {
        status: 500,
        headers: { "Content-Type": "application/json", ...corsHeaders }
      });
    }
  }
};
