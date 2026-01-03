// USAC10: Componente LightSigns
const int LED_PINS[4][3] = {
    {2, 3, 4},   // Trilho 1: R, Y, G
    {5, 6, 7},   // Trilho 2: R, Y, G
    {8, 9, 10},  // Trilho 3: R, Y, G
    {11, 12, 13} // Trilho 4: R, Y, G
};

enum LedState { OFF, ON, BLINKING };
LedState track_states[4] = {OFF, OFF, OFF, OFF};

bool blink_state = false;
unsigned long last_blink = 0;

void setup() {
    Serial.begin(9600);

    for (int track = 0; track < 4; track++) {
        for (int color = 0; color < 3; color++) {
            pinMode(LED_PINS[track][color], OUTPUT);
            digitalWrite(LED_PINS[track][color], LOW);
        }
    }

    pinMode(LED_BUILTIN, OUTPUT);
    Serial.println("USAC10-LIGHTSIGNS: Pronto para comandos");
}

void loop() {
    if (Serial.available()) {
        String cmd = Serial.readStringUntil('\n');
        cmd.trim();
        process_light_command(cmd);
    }

    update_leds();
    delay(50);
}

void process_light_command(String cmd) {
    // Formato: CMD,TT (ex: RE,01)
    if (cmd.length() < 4) {
        Serial.println("ERROR: FORMATO_INVALIDO");
        return;
    }

    String cmd_type = cmd.substring(0, 2);
    int track_num = cmd.substring(3).toInt();

    if (track_num < 1 || track_num > 4) {
        Serial.println("ERROR: TRILHO_INVALIDO");
        return;
    }

    int track_idx = track_num - 1;
    turn_off_track(track_idx);

    if (cmd_type == "RE") {
        digitalWrite(LED_PINS[track_idx][0], HIGH);
        track_states[track_idx] = ON;
    }
    else if (cmd_type == "YE") {
        digitalWrite(LED_PINS[track_idx][1], HIGH);
        track_states[track_idx] = ON;
    }
    else if (cmd_type == "GE") {
        digitalWrite(LED_PINS[track_idx][2], HIGH);
        track_states[track_idx] = ON;
    }
    else if (cmd_type == "RB") {
        track_states[track_idx] = BLINKING;
    }
    else {
        Serial.println("ERROR: COMANDO_DESCONHECIDO");
    }
}

void turn_off_track(int track) {
    for (int i = 0; i < 3; i++) {
        digitalWrite(LED_PINS[track][i], LOW);
    }
}

void update_leds() {
    unsigned long now = millis();

    if (now - last_blink >= 500) {
        last_blink = now;
        blink_state = !blink_state;

        for (int track = 0; track < 4; track++) {
            if (track_states[track] == BLINKING) {
                digitalWrite(LED_PINS[track][0], blink_state ? HIGH : LOW);
            }
        }

        digitalWrite(LED_BUILTIN, blink_state ? HIGH : LOW);
    }
}