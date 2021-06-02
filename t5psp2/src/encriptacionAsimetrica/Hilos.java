package encriptacionAsimetrica;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
public class Hilos extends Thread
{
	String texto = "";
	DataInputStream fentrada;
	Socket socket;
	boolean fin = false;
	public Hilos(Socket socket)
	{
		this.socket = socket;
		try
		{
			fentrada = new DataInputStream(socket.getInputStream());
		}
		
		catch (IOException e)
		{
			System.out.println("Error de E/S");
			e.printStackTrace();
		}
	}
	// En el método run() lo primero que hacemos
	// es enviar todos los mensajes actuales al cliente que se
	// acaba de incorporar
	public void run()
	{
		Servidor.mensaje.setText("Número de conexiones actuales: " + Servidor.ACTUALES);
		//String texto = Servidor.textarea.getText();
		//EnviarMensajes(texto);
		// Seguidamente, se crea un bucle en el que se recibe lo que el cliente escribe en el chat.
		// Cuando un cliente finaliza con el botón Salir, se envía un * al servidor del Chat,
		// entonces se sale del bucle while, ya que termina el proceso del cliente,
		// de esta manera se controlan las conexiones actuales
		while(!fin)
		{
			String cadena = "";
			try
			{
				cadena = fentrada.readUTF();
				String cadenaDesencriptada = desencriptacion(cadena);
				if(cadena.trim().equals("*"))
				{
					Servidor.ACTUALES--;
					Servidor.mensaje.setText("Número de conexiones actuales: "
							+ Servidor.ACTUALES);
					
					fin=true;
				}
				// El texto que el cliente escribe en el chat,
				// se añade al textarea del servidor y se reenvía a todos los clientes
				else
				{
					Servidor.textarea.append(cadenaDesencriptada + "\n");
					//texto = Servidor.textarea.getText();
					EnviarMensajes(cadena);
				}
			}
			
			catch (Exception ex)
			{
				ex.printStackTrace();
				fin=true;
			}
		}
	}
	// El método EnviarMensajes() envía el texto del textarea a
	// todos los sockets que están en la tabla de sockets,
	// de esta forma todos ven la conversación.
	// El programa abre un stream de salida para escribir el texto en el socket
	private void EnviarMensajes(String texto)
	{
		for(int i=0; i<Servidor.CONEXIONES; i++)
		{
			Socket socket = Servidor.tabla[i];
			try
			{
				DataOutputStream fsalida = new
						DataOutputStream(socket.getOutputStream());
				fsalida.writeUTF(texto);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}	
	
	
	public String desencriptacion(String mensaje) 
	{
		String resultado = "";
		try
		{			
			// Trabajamos con las claves privadas y públicas
			RSA rsa = new RSA();
			rsa.genKeyPair(512);
			rsa.openFromDiskPrivateKey("rsa.pri");
			rsa.openFromDiskPublicKey("rsa.pub");
			String encriptado = mensaje;
			String desencriptado = rsa.Decrypt(encriptado);
			
			System.out.println("Recibe mensaje");
			System.out.println("Texto encriptado: "+ encriptado);
			System.out.println("Texto desncriptado: "+ desencriptado+ "\n");
			System.out.println("Reenvía mensaje");
			System.out.println("Texto encriptado: "+ encriptado+ "\n");
			
			resultado = desencriptado;			
		}
		catch (Exception e)
		{
			System.out.println(e.getMessage());
		}
		
		return resultado;
	}
}