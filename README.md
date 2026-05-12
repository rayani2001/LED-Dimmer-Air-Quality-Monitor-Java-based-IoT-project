# 💡 LED Dimmer + Air Quality Monitor

> Java Swing GUI ↔ Arduino Uno via USB Serial — control LED brightness and monitor air quality in real-time.

**Student:** Rayani Minoli Warnakulasuriya Don Fernando  
**Course:** Course Projects 2: Java — Riga Nordic University  

---

## Overview

- 🔴 **LED control** — Java slider sends PWM commands to Arduino Pin 9 (0–100%)
- 🌬️ **Air quality** — MQ sensor reads analog values (0–1023) every 2 seconds
- 🚨 **Alert system** — color-coded bar: green / orange / red based on gas level
- 📋 **Serial log** — all sent and received messages shown live in the GUI

---

## Hardware

| # | Component | Spec | Purpose |
|---|-----------|------|---------|
| 1 | Arduino Uno | Keyestudio | Main control board |
| 2 | Red LED | 5mm | Brightness output |
| 3 | Resistor | 220 Ω | Current limiting |
| 4 | MQ Gas Sensor | Keyestudio blue | Air quality |
| 5 | Breadboard | 830-point | Prototyping |
| 6 | Jumper Wires | M-M | Connections |
| 7 | USB Cable | USB-A to USB-B | Serial + power |

---

## Wiring

| From | → | To | Note |
|------|---|----|------|
| `Arduino Pin 9` | → | `220Ω` → `LED Anode (+)` | PWM brightness |
| `Arduino GND` | → | `LED Cathode (−)` | LED ground |
| `MQ GND` | → | `Arduino GND` | Sensor ground |
| `MQ VCC` | → | `Arduino 5V` | Sensor power |
| `MQ AO` | → | `Arduino A0` | Analog reading |
| `MQ DO` | — | Not connected | Unused |

---

## Serial Protocol

| Direction | Command | Example | Response |
|-----------|---------|---------|----------|
| Java → Arduino | `BRIGHT:n` | `BRIGHT:75` | `OK:75` |
| Java → Arduino | `STATUS` | `STATUS` | `GAS:487` |
| Arduino → Java | `GAS:n` | `GAS:487` | Update gas panel |
| Arduino → Java | `READY` | `READY` | Logged on boot |

Gas levels: **0–199** = 🟢 Clean · **200–499** = 🟠 Moderate · **500+** = 🔴 HIGH LEVEL DETECTED

---

## How to Run

**Step 1 — Upload Arduino sketch**
1. Open `arduino/sketch.ino` in Arduino IDE
2. Select Board: `Arduino Uno` and your COM port
3. Click Upload → open Serial Monitor at 9600 baud — you should see `READY`

**Step 2 — Add jSerialComm to IntelliJ**
1. File → Project Structure → Libraries → `+` → From Maven
2. Search: `com.fazecast:jSerialComm` → select latest → OK

**Step 3 — Run the GUI**
1. Open `java-app/src/leddimmer/MainWindow.java`
2. Right-click → Run `MainWindow.main()`
3. Select your COM port → Connect → done!

---

## Folder Structure

project/
├── arduino/
│   ├── sketch.ino
│   └── diagram.json
├── java-app/
│   └── src/leddimmer/
│       ├── MainWindow.java
│       └── SerialManager.java
├── docs/
│   ├── index.html
│   ├── images/
│   └── screenshots/
├── assets/
└── README.md


---

## Links

| | URL |
|-|-----|
| 🔗 GitHub Repo | [github.com/rayani2001/LED-Dimmer-Air-Quality-Monitor-Java-based-IoT-project](https://github.com/rayani2001/LED-Dimmer-Air-Quality-Monitor-Java-based-IoT-project) |
| 🌐 GitHub Pages | [rayani2001.github.io/LED-Dimmer-Air-Quality-Monitor-Java-based-IoT-project](https://rayani2001.github.io/LED-Dimmer-Air-Quality-Monitor-Java-based-IoT-project/) |
| 🔬 Wokwi Sim | [wokwi.com/projects/463165937757162497](https://wokwi.com/projects/463165937757162497) |

---

*Riga Nordic University · Course Projects 2: Java · 2026*
