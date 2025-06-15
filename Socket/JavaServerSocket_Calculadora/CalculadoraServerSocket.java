import java.io.*;
import java.net.*;
import java.util.*;

public class CalculadoraServerSocket {
    private static final int PORT = 9090;
    private static final Set<String> processedOperations = Collections.synchronizedSet(new HashSet<>());
    private static final Calculadora calc = new Calculadora();

    public static void main(String[] args) {
        try (ServerSocket welcomeSocket = new ServerSocket(PORT)) {
            System.out.println("Servidor calculadora iniciado na porta " + PORT);
            System.out.println("Aguardando conexões...\n");

            while (true) { // FIca procurando por mensagens de clientes
                try {
                    Socket connectionSocket = welcomeSocket.accept();
                    new ClientHandler(connectionSocket).start();
                } catch (IOException e) {
                    System.err.println("Erro ao aceitar conexão: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Não foi possível iniciar o servidor na porta " + PORT);
            e.printStackTrace();
        }
    }

    private static class ClientHandler extends Thread {
        private final Socket connectionSocket;

        public ClientHandler(Socket socket) {
            this.connectionSocket = socket;
        }

        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
                 OutputStream out = connectionSocket.getOutputStream()) {

                // Ler dados da operação
                String operationId = in.readLine();
                String operationStr = in.readLine();
                String oper1Str = in.readLine();
                String oper2Str = in.readLine();

                // Verificar se operação já foi processada
                if (processedOperations.contains(operationId)) {
                    out.write(("ERRO: Operação duplicada\n").getBytes());
                    return;
                }
                processedOperations.add(operationId);

                // Validar e processar a operação
                double result = processOperation(operationStr, oper1Str, oper2Str);

                // Enviar resultado
                out.write((result + "\n").getBytes());
                System.out.printf("[%s] Operação: %s %s %s = %s%n",
                        new Date(), oper1Str, getOperationSymbol(operationStr), oper2Str, result);

            } catch (IOException e) {
                System.err.println("Erro na comunicação com cliente: " + e.getMessage());
            } catch (NumberFormatException e) {
                try {
                    connectionSocket.getOutputStream().write(("ERRO: Números inválidos\n").getBytes());
                } catch (IOException ioException) {
                    System.err.println("Erro ao enviar mensagem de erro: " + ioException.getMessage());
                }
            } catch (IllegalArgumentException e) {
                try {
                    connectionSocket.getOutputStream().write(("ERRO: " + e.getMessage() + "\n").getBytes());
                } catch (IOException ioException) {
                    System.err.println("Erro ao enviar mensagem de erro: " + ioException.getMessage());
                }
            } finally {
                try {
                    connectionSocket.close();
                } catch (IOException e) {
                    System.err.println("Erro ao fechar socket: " + e.getMessage());
                }
            }
        }

        private double processOperation(String operationStr, String oper1Str, String oper2Str) {
            int operation;
            try {
                operation = Integer.parseInt(operationStr);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Operação inválida");
            }

            double num1 = Double.parseDouble(oper1Str);
            double num2 = Double.parseDouble(oper2Str);

            switch (operation) {
                case 1: // Soma
                    return calc.soma(num1, num2);
                case 2: // Subtração
                    return calc.subtrai(num1, num2);
                case 3: // Multiplicação
                    return calc.multiplica(num1, num2);
                case 4: // Divisão
                    if (num2 == 0) {
                        throw new IllegalArgumentException("Divisão por zero");
                    }
                    return calc.divide(num1, num2);
                default:
                    throw new IllegalArgumentException("Operação não suportada");
            }
        }

        private String getOperationSymbol(String operationStr) {
            switch (operationStr) {
                case "1": return "+";
                case "2": return "-";
                case "3": return "*";
                case "4": return "/";
                default: return "?";
            }
        }
    }
}