import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

// Implementação da calculadora remota
public class Calculadora implements ICalculadora {

	private static int chamadas = 0; // Contador simples de chamadas independente de qual operação for chamada

    // Implementação do método de soma
    public int soma(int a, int b) throws RemoteException {
        chamadas++;
        System.out.println("Método soma chamado " + chamadas + " vezes");
        return a + b;
    }

    // Implementação do método de subtração
    public int subtrai(int a, int b) throws RemoteException {
        chamadas++;
        System.out.println("Método subtrai chamado " + chamadas + " vezes");
        return a - b;
    }

    // Implementação do método de multiplicação
    public int multiplica(int a, int b) throws RemoteException {
        chamadas++;
        System.out.println("Método multiplica chamado " + chamadas + " vezes");
        return a * b;
    }

    // Implementação do método de divisão
    public double divide(int a, int b) throws RemoteException, ArithmeticException {
        chamadas++;
        System.out.println("Método divide chamado " + chamadas + " vezes");
        if (b == 0) {
            throw new ArithmeticException("Divisão por zero!");
        }
        return (double) a / b;
    }

    // Método principal para iniciar o servidor RMI
    public static void main(String[] args) {
        try {
            // Cria uma instância da calculadora
            Calculadora calculadora = new Calculadora();
            
            // Exporta o objeto para que possa receber chamadas remotas  
            ICalculadora stub = (ICalculadora) UnicastRemoteObject.exportObject(calculadora, 1100);
            
            // Obtém ou cria o registro RMI na porta 1099 
            Registry registro;
            try {
                System.out.println("Criando registro RMI...");
                registro = LocateRegistry.createRegistry(1099);
            } catch (RemoteException e) {
                System.out.println("Registro já existe, conectando...");
                registro = LocateRegistry.getRegistry(1099);
            }
            
            // Registra a calculadora no registro RMI com o nome "calculadora"
            registro.rebind("calculadora", stub);
            
            System.out.println("Servidor Calculadora RMI pronto!");
            System.out.println("Serviço registrado como 'calculadora'");
            
        } catch (Exception e) {
            System.err.println("Erro no servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }
}