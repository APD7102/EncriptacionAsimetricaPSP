package encriptacionAsimetrica;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;


public class Cliente extends JFrame implements ActionListener
{
	private static final long serialVersionUID = 1L;
	Socket socket;
	DataInputStream input;
	DataOutputStream output;
	String nombre;
	static JTextField mensaje = new JTextField();
	private JScrollPane sc;
	static JTextArea textarea;
	JButton boton = new JButton("Enviar");
	JButton salir = new JButton("Salir");
	boolean repetir = true;
	static boolean repetir2 = true;
	
	public Cliente(Socket socket, String nombre)
	
	{
		// Prepara la pantalla. Se recibe el socket creado y el nombre del cliente
		super(" Conexión del cliente: " + nombre);
		setLayout(null);
		mensaje.setBounds(10, 10, 400, 30);
		add(mensaje);
		textarea = new JTextArea();
		sc = new JScrollPane(textarea);
		sc.setBounds(10, 50, 400, 300);
		add(sc);
		boton.setBounds(420, 10, 100, 30);
		add(boton);
		salir.setBounds(420, 50, 100, 30);
		add(salir);
		textarea.setEditable(false);
		boton.addActionListener(this);
		this.getRootPane().setDefaultButton(boton);
		salir.addActionListener(this);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.socket = socket;
		this.nombre = nombre;
		// Se crean los flujos de entrada y salida.
		// En el flujo de salida se escribe un mensaje
		// indicando que el cliente se ha unido al Chat.
		// El HiloServidor recibe este mensaje y
		// lo reenvía a todos los clientes conectados
		try
		{
			input = new DataInputStream(socket.getInputStream());
			output = new DataOutputStream(socket.getOutputStream());
			String texto = "SERVIDOR>" + nombre + " ha entrado";
			output.writeUTF(encriptacion(texto));
		}
		catch (IOException ex)
		{
			System.out.println("Error de E/S");
			ex.printStackTrace();
			System.exit(0);
		}
	}
	
	// El método main es el que lanza el cliente,
	// para ello en primer lugar se solicita el nombre o nick del
	// cliente, una vez especificado el nombre
	// se crea la conexión al servidor y se crear la pantalla del Chat(ClientChat)
	// lanzando su ejecución (ejecutar()).
	public static void main(String[] args) throws Exception
	{
		do {
			int puerto = 44444;
			String nombre = JOptionPane.showInputDialog("Introduce tu nickname:");
			if(!nombre.trim().equals("")) 
			{
				Socket socket = null;
				try
				{
					socket = new Socket("127.0.0.1", puerto);
				}
				catch (IOException ex)
				{
					ex.printStackTrace();
					JOptionPane.showMessageDialog(null, "Imposible conectar con el servidor \n" + ex.getMessage(), "<<Mensaje de Error:1>>", JOptionPane.ERROR_MESSAGE);
					System.exit(0);
				}

				Cliente cliente = new Cliente(socket, nombre);
				cliente.setBounds(0,0,540,400);
				cliente.setVisible(true);
				cliente.ejecutar();
				repetir2 = false;

			}		
			else
			{
				JOptionPane.showMessageDialog(null,"El nombre está vacío");
				System.out.println("El nombre está vacío");
			}
		}while (repetir2);
	}
	// Cuando se pulsa el botón Enviar,
	// el mensaje introducido se envía al servidor por el flujo de salida
	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource()==boton)
		{
			String texto = nombre + "> " + mensaje.getText();
			
			try
			{
				mensaje.setText("");
				output.writeUTF(encriptacion(texto));
			}
			catch (IOException ex)
			{
				ex.printStackTrace();
			}
		}
		// Si se pulsa el botón Salir,
		// se envía un mensaje indicando que el cliente abandona el chat
		// y también se envía un * para indicar
		// al servidor que el cliente se ha cerrado
		else if(e.getSource()==salir)
		{
			String texto = "SERVIDOR>" + nombre + " ha abandonado";
			try
			{
				output.writeUTF(encriptacion(texto));
				output.writeUTF("*");
				repetir = false;
			}
			catch (IOException ex)
			{
				ex.printStackTrace();
			}
		}
	}
	// Dentro del método ejecutar(), el cliente lee lo que el
	// hilo le manda (mensajes del Chat) y lo muestra en el textarea.
	// Esto se ejecuta en un bucle del que solo se sale
	// en el momento que el cliente pulse el botón Salir
	// y se modifique la variable repetir
	public void ejecutar()
	{
		String texto = "";
		while(repetir)
		{
			try 			
			{
				texto = input.readUTF();
				String textodesencriptado =desencriptacion(texto);
				textarea.append(textodesencriptado+ "\n");
			}
			catch (IOException ex)
			{
				JOptionPane.showMessageDialog(null, "Imposible conectar con	el servidor \n" + ex.getMessage(), "<<Mensaje de Error:2>>", JOptionPane.ERROR_MESSAGE);
				repetir = false;
			}
		}
		try
		{
			socket.close();
			System.exit(0);
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
		}
	}
	
	public String encriptacion(String mensaje) 
	{
		String resultado = "";
		try
		{
			
			// Creo el flujo de salida al servidor
			// Definimos un texto a cifrar
			String str = mensaje;			
			// Trabajamos con las claves privadas y públicas
			RSA rsa = new RSA();
			rsa.genKeyPair(512);
			rsa.saveToDiskPrivateKey("rsa.pri");
			rsa.saveToDiskPublicKey("rsa.pub");
			// Ciframos y e imprimimos, el texto cifrado
			// es devuelto en la variable secure
			String secure = rsa.Encrypt(str);
			
			System.out.println("Envia mensaje");
			System.out.println("Mensaje encriptado: " + secure);
			System.out.println("Mensaje desencriptado: " + str+ "\n");
			resultado = secure;	
			
		}
		catch (Exception e)
		{
			System.out.println(e.getMessage());
		}		
			
		return resultado;
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
			System.out.println("Texto desencriptado: "+ desencriptado+ "\n");
			
			resultado = desencriptado;
			
		}
		catch (Exception e)
		{
			System.out.println(e.getMessage());
		}
		
		return resultado;
	}
}