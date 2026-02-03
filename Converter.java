import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class Converter {

  static Listener question;
  static double amount = 0.0;
  static double conv_amount = 0.0;
  static double mmk_usd = 1/currencyFetcher("MMK");
  static double aud_usd = 1/currencyFetcher("AUD");
  static double yen_usd = 1/currencyFetcher("JPY");
  static double usd_mmk = currencyFetcher("MMK");
  static double usd_aud = currencyFetcher("AUD");
  static double usd_yen = currencyFetcher("JPY");


  // a method to create a button
  public static void addChoice(final String name) {
    // create a panel to contain button and label
    final JPanel choice =  new JPanel(new BorderLayout());	// create a border layout
    // create a button
    final JButton button = new JButton(name);
    // record the answer
    button.addActionListener(question);
    // add the button to the panel
    choice.add(button, BorderLayout.NORTH); // north orientation
    // add the panel(border layout) to the grid layout(QBox)
    question.add(choice);
  }


  // a method to choose currency type
  public static String askCurrType(final String text) {
    question = new Listener();
    // create one grid layout
    question.setLayout(new GridLayout(0,1));
    // add the label to the grid 
    // position the text(question) CENTER within the label
    question.add(new JLabel(text, JLabel.CENTER));
    // add five buttons(five panels) to the grid layout(QBox)
    addChoice("MMK");
    addChoice("AUD");
    addChoice("USD");
    addChoice("YEN");
    question.setModal(true); // dispose from memory = false
    question.pack(); // resize the box
    question.setLocationRelativeTo(null); // center the box to screen
    question.setVisible(true); // show the box
    if ((question.answer.equals("MMK")) || (question.answer.equals("AUD")) || (question.answer.equals("USD")) || (question.answer.equals("YEN"))) { // check the correct answer
      return question.answer;
    }
    else {
      JOptionPane.showMessageDialog(null, "Invalid currency");
      askCurrType(text);
    }
    return "USD";
  }


  // a method to get the numerical value of the currency
  public static double askAmount(){
    amount = Double.parseDouble(JOptionPane.showInputDialog("What is the amount?"));
    if (amount >= 0.0) {
      return amount;
    }
    else{
      JOptionPane.showMessageDialog(null, "Invalid amount");
      askAmount();
    }
    return 0.0;
  }


  // a method to convert the currency
  public static double currencyFetcher(final String currency) {
    // Build a string for the response
    final StringBuilder response = new StringBuilder();
    try {
      // Create a URL object for the currency API.
      final URL url = new URL("https://api.exchangerate-api.com/v6/latest");
      // Open a connection to the API.
      final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      // Set the request method to GET.
      connection.setRequestMethod("GET");
      // Read the response from the API.
      final BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
      String line;
      while ((line = reader.readLine()) != null) {
        response.append(line); // store values in a stringbuilder
      }
    }
    catch(final Exception e) {
      System.out.println(e);
    }
    final String responseStr = response.toString(); // convert stringbuilder to string
    final String[] arrOfStr = responseStr.split(","); // split the string into java array
    // create a dynamic array to delete unnecessary values
    final ArrayList<String> dynamicArr = new ArrayList<>();
    for (final String element : arrOfStr) // copy everything into a dynamic array
      dynamicArr.add(element);
    for (int i = 0; i < 12; i++)    // now delete unnecessary values
      dynamicArr.remove(0);
    // manipulate arrayList elements to get three exchange rates out of many options
    // store (AUD, JPY, MMK) in a new string array
    final String[] currRates = { dynamicArr.get(8).split(":")[1], dynamicArr.get(71).split(":")[1], dynamicArr.get(91).split(":")[1] };
    // convert these to double values
    final Double[] currRatesInDouble = new Double[3];
    int c = 0; // counter variable
    for (final String curr : currRates)
      currRatesInDouble[c++] = Double.parseDouble(curr);
    // retrieve currency rates according to input
    int index = 0;  // index variable
    if (currency == "AUD") index = 0;
    else if (currency == "JPY") index = 1;
    else if (currency == "MMK") index = 2;
    final double exchangeRate = currRatesInDouble[index];
    return exchangeRate;
  }


  // a method to show results
  public static void displayResults(final String input, final String input_curr, final String output, final String output_curr) {
    JOptionPane.showMessageDialog(null, input + " " + input_curr + " is equivalent to " + output + " " + output_curr);
  }


  // main function
  public static void main(final String[] args) {
    final String current_currency = askCurrType("Welcome to our service. \n Choose the name of YOUR currency below");
    amount = askAmount();
    final String desired_currency = askCurrType("Choose the type for conversion");
    // convert current currency to USD to make a standard system
    double usd = 0.0; // initiate USD 
    if (current_currency.equals("MMK")){
       usd = mmk_usd * amount;
    }
    else if (current_currency.equals("AUD")){
       usd = aud_usd * amount;
    }
    else if (current_currency.equals("USD")){
       usd = amount;
    }
    else if (current_currency.equals("YEN")){
       usd = yen_usd * amount;
    }

    // convert USD to required types
    if (desired_currency.equals("MMK")){
      conv_amount = usd_mmk * usd;
    }
    else if (desired_currency.equals("AUD")){
      conv_amount = usd_aud * usd;
    }
    else if (desired_currency.equals("USD")){
      conv_amount = usd;
    }
    else if (desired_currency.equals("YEN")){
      conv_amount = usd_yen * usd;
    }

    // display a rounded number (up to 2 decimal places)
    displayResults(String.format("%.2f", amount), current_currency, String.format("%.2f", conv_amount), desired_currency);
  }

}


