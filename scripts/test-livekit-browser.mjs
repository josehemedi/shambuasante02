/**
 * Test connexion LiveKit dans un vrai navigateur (Playwright + livekit-client).
 */
import { readFileSync } from "fs"
import { chromium } from "playwright"

const data = JSON.parse(
  readFileSync(new URL("./.tele-test-data.json", import.meta.url), "utf8")
)

const livekitClientPath = new URL(
  "../../shambua-sante-frontend-ui/node_modules/livekit-client/dist/livekit-client.umd.js",
  import.meta.url
).pathname.replace(/^\//, "")

async function testParticipant(label, token) {
  const browser = await chromium.launch({ headless: true })
  const page = await browser.newPage()

  try {
    await page.goto("about:blank")
    await page.addScriptTag({ path: livekitClientPath })

    const result = await page.evaluate(
      async ({ serverUrl, token, timeoutMs }) => {
        const room = new window.LivekitClient.Room()
        return new Promise((resolve, reject) => {
          const timer = setTimeout(() => {
            room.disconnect()
            reject(new Error(`timeout ${timeoutMs}ms`))
          }, timeoutMs)

          room
            .connect(serverUrl, token)
            .then(() => {
              clearTimeout(timer)
              const state = room.state
              room.disconnect()
              resolve({ state })
            })
            .catch((err) => {
              clearTimeout(timer)
              reject(new Error(err.message || String(err)))
            })
        })
      },
      { serverUrl: data.serverUrl, token, timeoutMs: 30000 }
    )

    console.log(`[OK] ${label} connecte - etat:`, result.state)
    return true
  } catch (err) {
    console.error(`[FAIL] ${label}:`, err.message)
    return false
  } finally {
    await browser.close()
  }
}

console.log("=== Test LiveKit navigateur (Playwright) ===")
console.log("Server:", data.serverUrl)
console.log("Room:", data.roomName)

const doctorOk = await testParticipant("Medecin", data.doctorToken)
const patientOk = await testParticipant("Patient", data.patientToken)

if (doctorOk && patientOk) {
  console.log("\n=== VIDEO LIVEKIT: CONNEXION REELLE REUSSIE ===")
  process.exit(0)
}

console.error("\n=== VIDEO LIVEKIT: ECHEC CONNEXION ===")
process.exit(1)
