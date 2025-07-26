import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

class GUIFrame extends JFrame implements ActionListener {
    
	private JLabel lineLabel = new JLabel("-------------------------------------------------------------------------------------------------------------------------------------------");
    private JLabel handingLabel = new JLabel("PEER TO PEER NETWORK BIG FILE TRANSFER");
    private JLabel heading2Label = new JLabel("Enter Resource Path");
    private JLabel heading3Label = new JLabel("Online Peers");

    private JTextField resPathTextField = new JTextField();
    private JTextField iptextTextField = new JTextField();

    private JButton browseButton = new JButton("BROWSE");
    private JButton transmitButton = new JButton("TRANSMIT");
    private JButton exitButton = new JButton("Exit");

    private JProgressBar progressBar = new JProgressBar();
    private JList<String> onlinePeersList = new JList<>();

    GUIFrame() {
        Font f1 = new Font("ALGERIAN", Font.BOLD, 25);
        Font f2 = new Font("BROADWAY", Font.BOLD, 16);
        Font f3 = new Font("ALGERIAN", Font.BOLD, 10);
        Font f5 = new Font("aril", Font.BOLD, 16);

        Container con=getContentPane();
		con.setLayout(null);
        con.setBackground(Color.cyan);

		setBounds(300, 50, 800, 500);
        
        handingLabel.setBounds(100, 40, 600, 30);
        handingLabel.setFont(f1);

        con.add(handingLabel);
        con.add(lineLabel);
        con.add(heading2Label);

        lineLabel.setBounds(100, 60, 600, 30);
        lineLabel.setFont(f3);

        heading2Label.setBounds(50, 100, 200, 70);
        heading2Label.setFont(f2);

        resPathTextField.setBounds(50, 150, 500, 35);
        con.add(resPathTextField);
        resPathTextField.setFont(f5);

        browseButton.setBounds(200, 250, 140, 50);
        con.add(browseButton);

        heading3Label.setBounds(600, 100, 270, 70);
        heading3Label.setFont(f2);
        con.add(heading3Label);

        iptextTextField.setBounds(320, 220, 230, 35);
        iptextTextField.setFont(f5);

        transmitButton.setBounds(50, 250, 140, 50);
        con.add(transmitButton);
        transmitButton.setFont(f2);
        browseButton.setFont(f2);

        progressBar.setBounds(50, 360, 500, 35);
        con.add(progressBar);
        progressBar.setStringPainted(true);

        exitButton.setBounds(270, 430, 100, 35);
        con.add(exitButton);
        exitButton.setFont(f2);
        exitButton.setBackground(Color.red);

        onlinePeersList.setBounds(570, 150, 200, 330);
        con.add(onlinePeersList);
        //onlinePeersList.addListSelectionListener(this);

        progressBar.setStringPainted(true);

        browseButton.addActionListener(this);
        transmitButton.addActionListener(this);
        exitButton.addActionListener(this);
    }//end constructor


    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == browseButton) {
            FileDialog fileDialog = new FileDialog(this, "Select File", FileDialog.LOAD);
            fileDialog.setVisible(true);
            String directory = fileDialog.getDirectory();
            String file = fileDialog.getFile();
           
            resPathTextField.setText(directory+file);
        }

        if (e.getSource() == transmitButton)
                new TransmitDataThread(this).start();

        if (e.getSource() == exitButton) {
            int code=JOptionPane.showConfirmDialog(this,"Are you sure to exit?","confirmation",JOptionPane.YES_NO_OPTION);
			if(code==JOptionPane.YES_OPTION)
               System.exit(0);
        }
    }//end action
	
    public String getIPAddress(){
	   return (String) onlinePeersList.getSelectedValue();
	}
    public String getSelectedFilePath(){
	  return resPathTextField.getText();    
    }
	
    public void setMaximum(int totalPackets){
	    progressBar.setMaximum(totalPackets);
    }
    public void setValue(int counter){
	    progressBar.setValue(counter);
    }
    public void setListData(java.util.Vector v){
	  onlinePeersList.setListData(v);
    }

}//end class

class TransmitDataThread extends Thread {
    private GUIFrame frame;

	TransmitDataThread(GUIFrame frame){
		this.frame=frame;
	}
	
    public void run(){
        try{
            Socket socket = new Socket(frame.getIPAddress(), 1010);
            System.out.println("Client Connected");

            FileInputStream fileInputStream = new FileInputStream(frame.getSelectedFilePath());
           
			DataInputStream in = new DataInputStream(socket.getInputStream());
			PrintStream out = new PrintStream(socket.getOutputStream());

            File selectedFile = new File(frame.getSelectedFilePath());
            String fileName = selectedFile.getName();
                   
			int size = fileInputStream.available();
			int packetSize = 1000;
			int totalPackets = size / packetSize;
			int lastPackets = size % packetSize;

            out.println(fileName); // Send only the file name
			out.println(size);
			out.println(packetSize);
			out.println(totalPackets);
			out.println(lastPackets);

            frame.setMaximum(totalPackets);
                    
			byte[] data = new byte[packetSize];
			for (int i = 1; i <= totalPackets; i++) {
				fileInputStream.read(data, 0, packetSize);
				out.write(data, 0, packetSize);
				frame.setValue(i);
			 }
		
			fileInputStream.read(data, 0, lastPackets);
			out.write(data, 0, lastPackets);

			out.flush();
			fileInputStream.close();
			out.close();
			socket.close();
			
            System.out.println("DATA WRITE SUCCESSFULLY");
            JOptionPane.showMessageDialog(frame, "DATA WRITE SUCCESSFULLY");    
        }catch (Exception e){
            e.printStackTrace();
			JOptionPane.showMessageDialog(frame, "Error: "+e.getMessage());
        }
	}
}// end transmit class 

class ReceiveDataThread extends Thread {
    private GUIFrame frame;
	
	ReceiveDataThread(GUIFrame frame){
		this.frame=frame;
	}

	public void run() {
        try {
            ServerSocket server = new ServerSocket(1010);
            System.out.println("File Receiver Server Started...");
			do {
                Socket socket = server.accept();
                System.out.println("Client Connected..");

				DataInputStream in = new DataInputStream(socket.getInputStream());
				PrintStream out = new PrintStream(socket.getOutputStream());

			   	String fileName = in.readLine(); // Receive the file name
				int Size = Integer.parseInt(in.readLine());
				int packetSize = Integer.parseInt(in.readLine());
				int totalpacketSize = Integer.parseInt(in.readLine());
				int lastpacketSize = Integer.parseInt(in.readLine());
			
				File dir=new File("downloaded-Files");
				dir.mkdirs();
			  
			    FileOutputStream fileOutputStream = new FileOutputStream("downloaded-Files/"+fileName);
 				byte[] data = new byte[packetSize];
				int bytesRead;

				for (int i = 0; i < totalpacketSize; i++) {
					in.read(data, 0, packetSize);
					fileOutputStream.write(data, 0, packetSize);     
					System.out.println("Downloaded Package: " + i);
				}

				in.read(data, 0, lastpacketSize);
				fileOutputStream.write(data, 0, lastpacketSize);
            

				fileOutputStream.close();
				in.close();
				out.close();
				socket.close();

				System.out.println("File successfully downloaded.");
			}while (true);
        }catch (Exception e) {
            e.printStackTrace();
			JOptionPane.showMessageDialog(frame, "Error: "+e.getMessage());
        }
    }//end run
}// end of ReceiveDataThread
   
 class HandleRegistryConn extends Thread {
    private GUIFrame frame;
	
	HandleRegistryConn(GUIFrame frame){
		this.frame=frame;
	}

    public void run() {
        try {
            Socket socket = new Socket("shobanPc", 9090);
			System.out.println("Connected with Registry Service...");

            DataInputStream in = new DataInputStream(socket.getInputStream());
            do {
                String clientList = in.readLine();
                System.out.println(clientList);

                java.util.Vector v = getClientList(clientList);
                frame.setListData(v);

                System.out.println("*****************************");
            } while (true);
        } catch (Exception e) {
            e.printStackTrace();
  		    JOptionPane.showMessageDialog(frame, "Error: "+e.getMessage());
        }
    }//end run method

    private java.util.Vector getClientList(String clientList) {
        java.util.StringTokenizer t = new java.util.StringTokenizer(clientList, ":");
        java.util.Vector v = new java.util.Vector();

        while (t.hasMoreElements()) {
            String hostName = (String) t.nextToken();
            v.addElement(hostName);
        }
        return v;
    }//end method
}//end HandleRegistryConn class
  
public class P2PBigDataShiftingAgent{
	
    public static void main(String arg[]) {

        GUIFrame frame = new GUIFrame();
        frame.setVisible(true);
		
		ReceiveDataThread t = new ReceiveDataThread(frame);
		t.start();

        HandleRegistryConn regCon = new HandleRegistryConn(frame);
        regCon.start();
    }
}//end main class
