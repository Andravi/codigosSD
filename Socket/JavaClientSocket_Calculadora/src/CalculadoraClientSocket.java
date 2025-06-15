import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class CalculadoraClientSocket {
    // Definindo constantes
    private static final String SERVER_IP = "0.0.0.0";
    private static final int SERVER_PORT = 9090;
    private static final int MAX_RETRIES = 3;
    private static final int BASE_RETRY_DELAY_MS = 1000;

    public static void main(String[] args) {
        System.out.println("=== Calculadora Cliente ===");
        System.out.println("Digite expressões como: '10 + 20' ou '(10 + 5) * 3'");
        System.out.println("Operações: + (soma), - (subtração), * (multiplicação), / (divisão)");
        System.out.println("Digite 'sair' para encerrar\n");

        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("> ");
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("sair")) {
                break;
            }

            try {
                double result = evaluateExpression(input); // Fazendo o teste da expressão e obtendo o resultado
                System.out.println("Resultado = " + result);
            } catch (Exception e) {
                System.out.println("Erro: " + e.getMessage());
            }
        }
        scanner.close();
    }

    private static double evaluateExpression(String expression) throws Exception {
        // Processa parênteses primeiro (avalia do mais interno para o mais externo) decompondo usando recursão e devolvendo o resultado no final
        while (true) {
            Matcher matcher = Pattern.compile("\\(([^()]+)\\)").matcher(expression);
            if (!matcher.find()) break;
            
            String subExpr = matcher.group(1);
            double subResult = evaluateSimpleExpression(subExpr);
            expression = expression.substring(0, matcher.start()) + subResult + expression.substring(matcher.end());
        }
        
        return evaluateSimpleExpression(expression);
    }

    // 
    private static double evaluateSimpleExpression(String expr) throws Exception {
        // Remove espaços em branco
        expr = expr.replaceAll("\\s+", "");
        
        // Separa números e operadores usando regex
        String[] tokens = expr.split("(?<=[-+*/])|(?=[-+*/])");
        
        if (tokens.length < 3 || tokens.length % 2 == 0) {
            throw new Exception("Expressão inválida");
        }

        // Primeiro processa multiplicações e divisões
        for (int i = 1; i < tokens.length; i += 2) {
            String op = tokens[i];
            if (op.equals("*") || op.equals("/")) {
                double num1 = Double.parseDouble(tokens[i-1]);
                double num2 = Double.parseDouble(tokens[i+1]);
                double result = sendToServer(op, num1, num2);
                
                // Substitui os 3 elementos (num1, op, num2) pelo resultado
                String[] newTokens = new String[tokens.length - 2];
                System.arraycopy(tokens, 0, newTokens, 0, i-1);
                newTokens[i-1] = String.valueOf(result);
                System.arraycopy(tokens, i+2, newTokens, i, tokens.length - i - 2);
                tokens = newTokens;
                i -= 2;
            }
        }
        
        // Depois processa adições e subtrações
        double result = Double.parseDouble(tokens[0]);
        for (int i = 1; i < tokens.length; i += 2) {
            String op = tokens[i];
            double num = Double.parseDouble(tokens[i+1]);
            result = sendToServer(op, result, num);
        }
        
        return result;
    }

    private static int getOperationCode(String op) {
        switch (op) {
            case "+": return 1;
            case "-": return 2;
            case "*": return 3;
            case "/": return 4;
            default: throw new IllegalArgumentException("Operação inválida: " + op);
        }
    }

    // manda a operação para o servidor Socket e retorna a resposta
    private static double sendToServer(String operation, double num1, double num2) throws Exception {
        int operationCode = getOperationCode(operation);
        int attempt = 0;
        Exception lastError = null;

        while (attempt < MAX_RETRIES) { // Tentativas de conexão com o server
            try {
                Socket clientSocket = new Socket(SERVER_IP, SERVER_PORT);
                DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                // Envia ID único para evitar processamento duplicado
                out.writeBytes(UUID.randomUUID().toString() + "\n");
                out.writeBytes(operationCode + "\n");
                out.writeBytes(num1 + "\n");
                out.writeBytes(num2 + "\n");
                out.flush();

                String response = in.readLine();
                clientSocket.close();

                if (response == null || response.startsWith("ERRO:")) {
                    throw new Exception(response != null ? response : "Resposta inválida do servidor");
                }

                return Double.parseDouble(response);

            } catch (IOException e) {
                lastError = e;
                attempt++;
                if (attempt < MAX_RETRIES) {
                    Thread.sleep(BASE_RETRY_DELAY_MS * attempt);
                }
            }
        }

        throw new Exception("Falha na comunicação com o servidor após " + MAX_RETRIES + " tentativas. Último erro: " + lastError.getMessage());
    }
}