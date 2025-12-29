// arduino/sensors/sensors.ino
#include <DHT.h>

#define DHTPIN 2
#define DHTTYPE DHT22

DHT dht(DHTPIN, DHTTYPE);

// Buffer circular para mediana móvel
#define BUFFER_SIZE 10
float tempBuffer[BUFFER_SIZE];
float humidBuffer[BUFFER_SIZE];
int bufferIndex = 0;
int bufferCount = 0;

void setup() {
    Serial.begin(9600);
    dht.begin();

    // Inicializar buffers
    for (int i = 0; i < BUFFER_SIZE; i++) {
        tempBuffer[i] = 0;
        humidBuffer[i] = 0;
    }
}

// Função para calcular mediana
float calculateMedian(float arr[], int n) {
    // Ordenar array (bubble sort simples)
    for (int i = 0; i < n-1; i++) {
        for (int j = 0; j < n-i-1; j++) {
            if (arr[j] > arr[j+1]) {
                float temp = arr[j];
                arr[j] = arr[j+1];
                arr[j+1] = temp;
            }
        }
    }

    // Calcular mediana
    if (n % 2 == 0) {
        return (arr[n/2 - 1] + arr[n/2]) / 2.0;
    } else {
        return arr[n/2];
    }
}

void loop() {
    // Ler dados do sensor
    float humidity = dht.readHumidity();
    float temperature = dht.readTemperature();

    // Verificar se leitura é válida
    if (isnan(humidity) || isnan(temperature)) {
        Serial.println("ERROR: Failed to read from DHT sensor!");
        delay(2000);
        return;
    }

    // Adicionar aos buffers
    tempBuffer[bufferIndex] = temperature;
    humidBuffer[bufferIndex] = humidity;
    bufferIndex = (bufferIndex + 1) % BUFFER_SIZE;
    if (bufferCount < BUFFER_SIZE) bufferCount++;

    // Calcular medianas
    float tempMedian = calculateMedian(tempBuffer, bufferCount);
    float humidMedian = calculateMedian(humidBuffer, bufferCount);

    // Formatar saída conforme especificação
    // Formato: TEMP&unit:celsius&value:XX#HUM&unit:percentage&value:XX
    Serial.print("TEMP&unit:celsius&value:");
    Serial.print((int)tempMedian);
    Serial.print("#HUM&unit:percentage&value:");
    Serial.println((int)humidMedian);

    delay(5000); // Ler a cada 5 segundos
}