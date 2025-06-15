import paho.mqtt.client as mqtt
import json
from collections import deque
import time
from datetime import datetime

# Configurações MQTT
MQTT_BROKER = "localhost"
MQTT_PORT = 1883
TOPIC_TEMPERATURE = "industrial/temperature/#"
TOPIC_AVERAGE = "industrial/average"
TOPIC_ALERTS = "industrial/alerts"

# Armazenamento de temperaturas (últimos 120 segundos)
temperature_readings = deque(maxlen=10)  # Assume 10 leituras (60s * 2)

def on_connect(client, userdata, flags, reason_code, properties):
    if reason_code == 0:
        print(f"[{timestamp()}] CAT conectado ao broker MQTT")
        client.subscribe(TOPIC_TEMPERATURE)
    else:
        print(f"[{timestamp()}] Falha na conexão. Código: {reason_code}")

def on_message(client, userdata, message):
    try:
        data = json.loads(message.payload.decode())
        store_reading(data)
        if len(temperature_readings) > 1:
            compute_and_publish(client)
            
    except Exception as e:
        print('=============================')

        print(f"[{timestamp()}] Erro ao processar mensagem: {e}")

def store_reading(data):
    """Armazena a leitura do sensor com timestamp"""
    temperature_readings.append({
        "temp": data["temperature"],
        "time": data["timestamp"],
        "sensor": data["sensor_id"]
    })
    
    # Remove leituras com mais de 120 segundos
    current_time = time.time()
    while temperature_readings and current_time - temperature_readings[0]["time"] > 120:
        temperature_readings.popleft()

def compute_and_publish(client):
    """Calcula médias e publica alertas se necessário"""
    temps = [r["temp"] for r in temperature_readings]
    avg_temp = sum(temps) / len(temps)
    
    # Publica média
    client.publish(TOPIC_AVERAGE, json.dumps({
        "average": round(avg_temp, 2),
        "readings": len(temps),
        "timestamp": time.time(),
        "sensors": list({r["sensor"] for r in temperature_readings})
    }))
    
    print(f"[{timestamp()}] Média publicada: {round(avg_temp, 2)}°C (de {len(temps)} leituras)")
    
    # Verifica alertas
    if len(temps) >= 4:  # Precisa de pelo menos 2 médias para comparação
        check_rapid_increase(client, temps)
    
    if avg_temp > 200:
        trigger_high_temp_alert(client, avg_temp)

def check_rapid_increase(client, temps):
    """Verifica aumento repentino de temperatura"""
    last_two_avg = sum(temps[-2:]) / 2
    previous_two_avg = sum(temps[-4:-2]) / 2
    
    if abs(last_two_avg - previous_two_avg) >= 5:
        client.publish(TOPIC_ALERTS, json.dumps({
            "type": "rapid_increase",
            "current_avg": round(last_two_avg, 2),
            "previous_avg": round(previous_two_avg, 2),
            "timestamp": time.time()
        }))
        print('=============================')

        print(f"[{timestamp()}] Alerta: Aumento repentino de temperatura (+{abs(last_two_avg - previous_two_avg):.1f}°C)")

def trigger_high_temp_alert(client, avg_temp):
    """Dispara alerta de temperatura alta"""
    client.publish(TOPIC_ALERTS, json.dumps({
        "type": "high_temperature",
        "average": round(avg_temp, 2),
        "timestamp": time.time()
    }))
    print('=============================')
    print(f"[{timestamp()}] Alerta: Temperatura alta ({avg_temp:.1f}°C)")

def timestamp():
    """Retorna timestamp formatado"""
    return datetime.now().strftime('%H:%M:%S')

def main():
    print(f"[{timestamp()}] === Iniciando CAT Service ===")
    print(f"[{timestamp()}] Tópicos:")
    print(f"[{timestamp()}]  - Subscrevendo: {TOPIC_TEMPERATURE}")
    print(f"[{timestamp()}]  - Publicando médias: {TOPIC_AVERAGE}")
    print(f"[{timestamp()}]  - Publicando alertas: {TOPIC_ALERTS}")
    
    client = mqtt.Client(mqtt.CallbackAPIVersion.VERSION2, "CAT_Service")
    client.on_connect = on_connect
    client.on_message = on_message
    
    try:
        client.connect(MQTT_BROKER, MQTT_PORT)
        client.loop_forever()
    except KeyboardInterrupt:
        print(f"\n[{timestamp()}] Desligando CAT Service...")
        client.disconnect()
    except Exception as e:
        print(f"[{timestamp()}] Erro fatal: {e}")
    finally:
        print(f"[{timestamp()}] CAT Service encerrado")

if __name__ == "__main__":
    main()