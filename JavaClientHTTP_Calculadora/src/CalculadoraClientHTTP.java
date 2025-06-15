import java.io.*;
import java.net.*;
import org.json.*;

public class CalculadoraClientHTTP {
    // Configurações do servidor
    private static final String SERVER_URL = "http://localhost:8001/calculadora.php";
    private static final int MAX_RETRIES = 3;
    private static final int RETRY_DELAY_MS = 1000;

    public static void main(String[] args) {
        testarTodasOperacoes();
    }

    private static void testarTodasOperacoes() {
        try {
            System.out.println("=== Teste da Calculadora ===");
            
            testarOperacao(10, 5, 1, "Soma");
            testarOperacao(10, 5, 2, "Subtração");
            testarOperacao(10, 5, 3, "Multiplicação");
            testarOperacao(10, 5, 4, "Divisão");
            testarOperacao(10, 0, 4, "Divisão por zero");
            
        } catch (Exception e) {
            System.out.println("Erro nos testes: " + e.getMessage());
        }
    }

    private static void testarOperacao(double a, double b, int op, String nomeOp) {
        try {
            JSONObject resposta = calcularComRetry(a, b, op);
            
            if (resposta.has("erro")) {
                System.out.printf("%s: %s → ERRO: %s\n", nomeOp, formatarOperacao(a, b, op), resposta.getString("erro"));
            } else {
                System.out.printf("%s: %s = %.2f\n", nomeOp, formatarOperacao(a, b, op), resposta.getDouble("resultado"));
            }
        } catch (Exception e) {
            System.out.printf("Falha na operação %s: %s\n", nomeOp, e.getMessage());
        }
    }

    private static String formatarOperacao(double a, double b, int op) {
        String[] simbolos = {"?", "+", "-", "*", "/"};
        return String.format("%.2f %s %.2f", a, simbolos[op], b);
    }

    public static JSONObject calcularComRetry(double oper1, double oper2, int operacao) throws Exception {
        int tentativas = 0;
        Exception ultimoErro = null;
        
        while (tentativas < MAX_RETRIES) {
            try {
                return calcular(oper1, oper2, operacao);
            } catch (IOException | JSONException e) {
                ultimoErro = e;
                tentativas++;
                System.out.println("Tentativa " + tentativas + " falhou. Tentando novamente...");
                if (tentativas < MAX_RETRIES) {
                    Thread.sleep(RETRY_DELAY_MS);
                }
            }
        }
        
        throw new Exception("Falha após " + MAX_RETRIES + " tentativas: " + ultimoErro.getMessage());
    }

    public static JSONObject calcular(double oper1, double oper2, int operacao) throws IOException, JSONException {
        HttpURLConnection conn = null;
        BufferedReader reader = null;
        
        try {
            // Configurar conexão
            URL url = new URL(SERVER_URL);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);
            
            // Enviar parâmetros
            String params = String.format("oper1=%f&oper2=%f&operacao=%d", oper1, oper2, operacao);
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = params.getBytes("utf-8");
                os.write(input, 0, input.length);
            }
            
            // Processar resposta
            int status = conn.getResponseCode();
            
            if (status == HttpURLConnection.HTTP_OK) {
                reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                return new JSONObject(response.toString());
            } else {
                reader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                StringBuilder errorResponse = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    errorResponse.append(line);
                }
                throw new IOException("HTTP Error " + status + ": " + errorResponse.toString());
            }
        } finally {
            if (reader != null) reader.close();
            if (conn != null) conn.disconnect();
        }
    }
}