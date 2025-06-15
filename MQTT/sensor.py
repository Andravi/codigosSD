import random
import time
import paho.mqtt.client as mqtt
import json
import threading

# Configurações MQTT
MQTT_BROKER = "localhost"
MQTT_PORT = 1883
TOPIC_TEMPERATURE = "industrial/temperature/"
SENSOR_COUNT = 3  # Número de sensores a serem simulados

def simulate_sensor(sensor_id):
    # Cria cliente com API versão 2
    client = mqtt.Client(mqtt.CallbackAPIVersion.VERSION2, f"Sensor_{sensor_id}")
    
    # Configura callbacks (opcional para sensores simples)
    client.on_connect = lambda c, u, f, rc, p: print(f"Sensor {sensor_id} conectado")
    client.on_disconnect = lambda c, u, rc: print(f"Sensor {sensor_id} desconectado")
    
    try:
        client.connect(MQTT_BROKER, MQTT_PORT)
        
        while True:
            # Gera temperatura entre 150 e 250 graus com flutuações aleatórias
            temp = round(180 + random.uniform(-30, 70), 2)
            payload = {
                "sensor_id": sensor_id,
                "temperature": temp,
                "timestamp": time.time()
            }
            
            client.publish(TOPIC_TEMPERATURE + str(sensor_id), json.dumps(payload))
            print(f"Sensor {sensor_id} enviou: {temp}°C")
            time.sleep(20)  # Envia a cada 60 segundos
            
    except Exception as e:
        print(f"Erro no sensor {sensor_id}: {e}")
    finally:
        client.disconnect()

if __name__ == "__main__":
    print(f"Iniciando {SENSOR_COUNT} sensores de temperatura...")
    
    # Inicia os sensores em threads separadas
    threads = []
    for i in range(1, SENSOR_COUNT + 1):
        thread = threading.Thread(target=simulate_sensor, args=(i,), daemon=True)
        thread.start()
        threads.append(thread)
    
    # Mantém o programa principal rodando
    try:
        while True:
            time.sleep(1)
    except KeyboardInterrupt:
        print("\nDesligando sensores...")
        for thread in threads:
            thread.join(timeout=1)