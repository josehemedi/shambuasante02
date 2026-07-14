/**
 * Test connexion LiveKit reelle (headless via @livekit/rtc-node) avec logs detailles.
 */
import { readFileSync } from "fs"
import { Room, RoomEvent, dispose } from "@livekit/rtc-node"

const dataPath = new URL("./.tele-test-data.json", import.meta.url)
const data = JSON.parse(readFileSync(dataPath, "utf8"))

async function testConnect(label, serverUrl, token) {
  const room = new Room()
  const events = []
  const timeout = 45000

  room.on(RoomEvent.Connected, () => events.push("Connected"))
  room.on(RoomEvent.Disconnected, (reason) => events.push(`Disconnected:${reason}`))
  room.on(RoomEvent.Reconnecting, () => events.push("Reconnecting"))
  room.on(RoomEvent.Reconnected, () => events.push("Reconnected"))
  room.on(RoomEvent.ConnectionStateChanged, (state) => events.push(`State:${state}`))

  try {
    await Promise.race([
      room.connect(serverUrl, token, { autoSubscribe: false }),
      new Promise((_, reject) =>
        setTimeout(() => reject(new Error(`timeout ${timeout}ms (${events.join(" -> ") || "no events"})`)), timeout)
      ),
    ])
    const state = room.connectionState
    await room.disconnect()
    return { label, state, events }
  } catch (err) {
    try {
      await room.disconnect()
    } catch {
      /* ignore */
    }
    throw new Error(`${label}: ${err.message}`)
  }
}

console.log("=== Test connexion LiveKit reelle ===")
console.log("Server:", data.serverUrl)
console.log("Room:", data.roomName)
console.log("RDV:", data.rdvId)

try {
  const doctor = await testConnect("Medecin", data.serverUrl, data.doctorToken)
  console.log("[OK] Medecin connecte - etat:", doctor.state, "|", doctor.events.join(" -> "))

  const patient = await testConnect("Patient", data.serverUrl, data.patientToken)
  console.log("[OK] Patient connecte - etat:", patient.state, "|", patient.events.join(" -> "))

  console.log("\n=== VIDEO LIVEKIT: CONNEXION REUSSIE ===")
  await dispose()
  process.exit(0)
} catch (err) {
  console.error("[FAIL]", err.message)
  console.error("\nNote: en environnement headless, WebRTC peut echouer (pare-feu/UDP).")
  console.error("Les tokens API sont valides si test-teleconsultation-api.ps1 passe.")
  await dispose()
  process.exit(1)
}
