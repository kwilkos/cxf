package demo.hw_https.common;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;


import org.objectweb.celtix.bus.configuration.security.SSLClientPolicy;
import org.objectweb.celtix.bus.configuration.security.SSLServerPolicy;

public final class DemoSecurityConfigurer {
    
    public void configure(SSLServerPolicy sslPolicyParam) {
        PasswordDialog pd = new PasswordDialog();
        pd.show();
        String pwd = new String(pd.passwordField.getPassword());
        sslPolicyParam.setKeystorePassword(pwd);
        sslPolicyParam.setKeyPassword(pwd);
    }

    public void configure(SSLClientPolicy sslPolicyParam) {

        PasswordDialog pd = new PasswordDialog();
        pd.show();
        String pwd = new String(pd.passwordField.getPassword());
        sslPolicyParam.setKeystorePassword(pwd);
        sslPolicyParam.setKeyPassword(pwd);
    }

}



class PasswordDialog extends JDialog {
    
    protected JPasswordField passwordField;
    
    private JLabel passwordLabel;
    private JButton loginBtn;
    
    
    
    public PasswordDialog() {
        super((JFrame)null);
        
        loginBtn = new JButton("OK");
        loginBtn.setMnemonic('o');
        loginBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

       
        passwordLabel = new JLabel();
        passwordField = new JPasswordField("", 20);    

        passwordLabel.setText("Password: ");
        passwordLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);

        setTitle("Please enter keystore passord.");
        setLocation(300, 250);
        setModal(true);
        getContentPane().setLayout(new BorderLayout());
        
        JPanel topPanel = new JPanel();
        
        setSize(400, 140);
        
        JPanel labelPanel = new JPanel();
        labelPanel.setLayout(new GridLayout(2, 1, 20, 20));

        passwordLabel.setText("Password:");
        labelPanel.add(passwordLabel);
        

        JPanel fieldsPanel = new JPanel();
        fieldsPanel.setLayout(new GridLayout(2, 1, 20, 20));

        passwordField.setNextFocusableComponent(loginBtn);
        fieldsPanel.add(passwordField);

        JPanel buttonsPanel = new JPanel();
        GridLayout glo = new GridLayout(1, 2);
        glo.setHgap(30);
        buttonsPanel.setLayout(glo);
        buttonsPanel.add(loginBtn);

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(buttonsPanel);
        
        topPanel.add(labelPanel, BorderLayout.WEST);
        topPanel.add(fieldsPanel, BorderLayout.CENTER);

        JPanel aPanel = new JPanel();
        aPanel.setLayout(new BorderLayout());
        aPanel.add(topPanel, BorderLayout.CENTER);

        getContentPane().add(aPanel, BorderLayout.NORTH);
        getContentPane().add(bottomPanel, BorderLayout.SOUTH);
    }
}
