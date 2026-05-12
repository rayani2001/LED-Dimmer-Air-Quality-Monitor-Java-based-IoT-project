// LED Dimmer + Air Quality Monitor
// Rayani Minoli Warnakulasuriya Don Fernando — Riga Nordic University

const int LED_PIN = 9;
const int GAS_PIN = A0;
unsigned long lastGasTime = 0;
String inputBuffer = "";

void setup() {
  Serial.begin(9600);
  pinMode(LED_PIN, OUTPUT);
  analogWrite(LED_PIN, 0);
  Serial.println("READY");
}

void loop() {
  if (millis() - lastGasTime >= 2000) {
    Serial.print("GAS:");
    Serial.println(analogRead(GAS_PIN));
    lastGasTime = millis();
  }
  while (Serial.available()) {
    char c = (char)Serial.read();
    if (c == '\n') {
      processCmd(inputBuffer);
      inputBuffer = "";
    } else {
      inputBuffer += c;
    }
  }
}

void processCmd(String cmd) {
  cmd.trim();
  if (cmd.startsWith("BRIGHT:")) {
    int pct = constrain(cmd.substring(7).toInt(), 0, 100);
    analogWrite(LED_PIN, map(pct, 0, 100, 0, 255));
    Serial.print("OK:");
    Serial.println(pct);
  } else if (cmd == "STATUS") {
    Serial.print("GAS:");
    Serial.println(analogRead(GAS_PIN));
  }
}
