import paho.mqtt.client as mqtt
import json
from datetime import datetime

# Configurações MQTT
MQTT_BROKER = "localhost"
MQTT_PORT = 1883
TOPIC_ALERTS = "industrial/alerts"

def on_connect(client, userdata, flags, reason_code, properties):
    if reason_code == 0:
        print(f"[{datetime.now().strftime('%H:%M:%S')}] Conectado ao broker MQTT")
        client.subscribe(TOPIC_ALERTS)
    else:
        print(f"[{datetime.now().strftime('%H:%M:%S')}] Falha na conexão. Código: {reason_code}")

def on_message(client, userdata, message):
    try:
        data = json.loads(message.payload.decode())
        timestamp = datetime.now().strftime('%H:%M:%S')
        
        if data["type"] == "rapid_increase":
            print(f"\n=== ALERTA [{timestamp}] ===")
            print("Tipo: Aumento repentino de temperatura")
            print(f"Média anterior: {data['previous_avg']}°C")
            print(f"Média atual: {data['current_avg']}°C")
            print("="*30)
        
        elif data["type"] == "high_temperature":
            print(f"\n=== ALERTA [{timestamp}] ===")
            print("Tipo: Temperatura alta")
            print(f"Temperatura média: {data['average']}°C")
            print("="*30)
        
    except Exception as e:
        print(f"[{datetime.now().strftime('%H:%M:%S')}] Erro ao processar mensagem: {e}")

def main():
    print("=== Serviço de Alarmes MQTT ===")
    print(f"Monitorando tópico: {TOPIC_ALERTS}")
    print("Aguardando alertas...\n")
    
    client = mqtt.Client(mqtt.CallbackAPIVersion.VERSION2, "AlarmService_Terminal")
    client.on_connect = on_connect
    client.on_message = on_message
    
    try:
        client.connect(MQTT_BROKER, MQTT_PORT)
        client.loop_forever()
    except KeyboardInterrupt:
        print("\nDesligando serviço de alarmes...")
        client.disconnect()
    except Exception as e:
        print(f"Erro fatal: {e}")
    finally:
        print("Serviço encerrado.")

if __name__ == "__main__":
    main()