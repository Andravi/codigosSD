import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ICalculadora extends Remote {
    public int soma(int a, int b) throws RemoteException;
    public int subtrai(int a, int b) throws RemoteException;
    public int multiplica(int a, int b) throws RemoteException;
    public double divide(int a, int b) throws RemoteException, ArithmeticException;
}