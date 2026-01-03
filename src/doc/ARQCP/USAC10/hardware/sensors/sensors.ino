#include <DHT.h>

// USAC10: Componente Sensors com filtro de mediana
#define DHT_PIN 15
#define DHT_TYPE DHT11
#define MEDIAN_WINDOW 5

DHT dht(DHT_PIN, DHT_TYPE);

// Buffer para mediana móvel (usando função assembly USAC09)
float temp_buffer[MEDIAN_WINDOW];
float hum_buffer[MEDIAN_WINDOW];
int buffer_index = 0;

void setup() {
    Serial.begin(9600);
    dht.begin();
    Serial.println("USAC10-SENSORS: Aguardando comando GTH");
}

void loop() {
    if (Serial.available()) {
        String cmd = Serial.readStringUntil('\n');
        cmd.trim();

        if (cmd == "GTH") {
            send_filtered_sensor_data();
        }
    }
    delay(100);
}

void send_filtered_sensor_data() {
    float temp = dht.readTemperature();
    float hum = dht.readHumidity();

    if (isnan(temp) || isnan(hum)) {
        Serial.println("ERROR: SENSOR_READ_FAIL");
        return;
    }

    // Adicionar ao buffer circular
    update_buffer(temp_buffer, temp);
    update_buffer(hum_buffer, hum);

    // Calcular mediana (simulação - em C real usaria USAC09)
    float filtered_temp = calculate_median_simple(temp_buffer);
    float filtered_hum = calculate_median_simple(hum_buffer);

    // Formato exigido: TEMP&unit:celsius&value:XX#HUM&unit:percent&value:YY
    String data = "TEMP&unit:celsius&value:";
    data += String((int)round(filtered_temp));
    data += "#HUM&unit:percent&value:";
    data += String((int)round(filtered_hum));

    Serial.println(data);
}

void update_buffer(float* buffer, float value) {
    buffer[buffer_index] = value;
    buffer_index = (buffer_index + 1) % MEDIAN_WINDOW;
}

float calculate_median_simple(float* buffer) {
    // Implementação simples - no Manager usaria função assembly USAC09
    float temp[MEDIAN_WINDOW];
    int count = 0;

    for (int i = 0; i < MEDIAN_WINDOW; i++) {
        if (buffer[i] != 0.0) temp[count++] = buffer[i];
    }

    if (count == 0) return 0.0;

    // Bubble sort simples
    for (int i = 0; i < count - 1; i++) {
        for (int j = 0; j < count - i - 1; j++) {
            if (temp[j] > temp[j + 1]) {
                float swap = temp[j];
                temp[j] = temp[j + 1];
                temp[j + 1] = swap;
            }
        }
    }

    if (count % 2 == 0) {
        return (temp[count/2 - 1] + temp[count/2]) / 2.0;
    } else {
        return temp[count/2];
    }
}