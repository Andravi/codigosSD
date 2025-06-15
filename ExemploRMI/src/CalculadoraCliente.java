import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

public class CalculadoraCliente {
    
    public static void main(String[] args) {
        // Configurações de conexão
        final String SERVER_HOST = "localhost";
        final int RMI_PORT = 1099;
        final String SERVICE_NAME = "calculadora";
        
        try {
            // 1. Conecta ao registry RMI no servidor
            System.out.println("Conectando ao servidor RMI...");
            Registry registry = LocateRegistry.getRegistry(SERVER_HOST, RMI_PORT);
            
            // 2. Obtém a referência remota da calculadora
            ICalculadora calculadora = (ICalculadora) registry.lookup(SERVICE_NAME);
            System.out.println("Conexão estabelecida com sucesso!");
            
            // 3. Interface interativa com o usuário
            Scanner scanner = new Scanner(System.in);
            boolean continuar = true;
            
            System.out.println("\n=== Calculadora Remota ===");
            System.out.println("Operações disponíveis:");
            System.out.println("1. Soma (+)");
            System.out.println("2. Subtração (-)");
            System.out.println("3. Multiplicação (*)");
            System.out.println("4. Divisão (/)");
            System.out.println("0. Sair");
            
            while (continuar) {
                System.out.print("\nEscolha uma operação (1-4) ou 0 para sair: ");
                int opcao = scanner.nextInt();
                
                if (opcao == 0) {
                    continuar = false;
                    continue;
                }
                
                System.out.print("Digite o primeiro número: ");
                int num1 = scanner.nextInt();
                
                System.out.print("Digite o segundo número: ");
                int num2 = scanner.nextInt();
                
                try {
                    double resultado = 0;
                    String operacao = "";
                    
                    // Executa a operação remota selecionada
                    switch (opcao) {
                        case 1:
                            resultado = calculadora.soma(num1, num2);
                            operacao = "+";
                            break;
                        case 2:
                            resultado = calculadora.subtrai(num1, num2);
                            operacao = "-";
                            break;
                        case 3:
                            resultado = calculadora.multiplica(num1, num2);
                            operacao = "*";
                            break;
                        case 4:
                            resultado = calculadora.divide(num1, num2);
                            operacao = "/";
                            break;
                        default:
                            System.out.println("Opção inválida!");
                            continue;
                    }
                    
                    // Exibe o resultado formatado
                    System.out.printf("\nResultado: %d %s %d = %.2f\n", 
                                     num1, operacao, num2, resultado);
                    
                } catch (ArithmeticException e) {
                    System.out.println("Erro matemático: " + e.getMessage());
                } catch (RemoteException e) {
                    System.out.println("Erro na comunicação remota: " + e.getMessage());
                }
            }
            
            scanner.close();
            System.out.println("Calculadora encerrada.");
            
        } catch (NotBoundException e) {
            System.out.println("Serviço não encontrado: " + e.getMessage());
        } catch (RemoteException e) {
            System.out.println("Erro ao conectar ao servidor: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Erro inesperado: " + e.getMessage());
        }
    }
}