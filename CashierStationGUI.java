import javafx.application.Application;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.text.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.TextFormatter.Change;
import java.io.*;
import java.util.*;
import java.util.function.*;
import javafx.collections.*;
import java.text.NumberFormat;

public class CashierStationGUI extends Application{
	//Screen size
	final int SCREEN_WIDTH = 800;
	final int SCREEN_HEIGHT = 600;
	
	//Colors to select from in SKU edit
	private List<String> colorList = Arrays.asList("Red", "Orange", "Yellow", "Green", "Blue", "Purple", "White", "Peach", "Cream", "Lavendar", "Periwinkle", "Pink", "Sea Foam", "Magenta");
	ObservableList<String> color = FXCollections.observableArrayList(colorList);
	
	//Populate store settings
	StoreSettings myStore = new StoreSettings();
	
	//Populate array of customers
	ArrayList<Customer> customers = new ArrayList<>();
	ObservableList<Customer> data = FXCollections.observableArrayList();
	
	//Populate inventory
	ArrayList<SKU> inventory = new ArrayList<>();
	ObservableList<SKU> inventoryData = FXCollections.observableArrayList();
	
	
	@Override
	public void start(Stage primaryStage) {
		//Import store settings, inventory data, and customer data
		try {
			myStore.readStoreSettingsFromFile();
			customers = inputCustomersFromDataBase();
			inventory = readInventoryFromFile();
		} catch (Exception ex) { 
			System.out.println(ex);
		}
		data = FXCollections.observableArrayList(customers);
		inventoryData = FXCollections.observableArrayList(inventory);

		//Load home screen
		loadHomeScreen(primaryStage);
	}
	
	@Override
	public  void stop() {
		try {
			myStore.saveStoreSettingsToFile();
			exportInventoryToFile(inventory);
			exportCustomersToDataBase(customers);
			System.out.println("Data exported successfully.");
		} catch (IOException ex) {
			System.out.println("Data export failed.");
		}
		System.out.println("Stage is closing.");
	}
	
	public static void main(String[] args) {
		Application.launch(args);
	}
	
	public boolean passwordVerificationWindow(String message) {
		myStore.setAdminPassword("1234");
		
		//Create layout elements
		PasswordField passwordField = new PasswordField();
		passwordField.setMaxWidth(160);
		Button btnCancel = new Button("Cancel");
		Button btnSubmit = new Button("Submit");
		Label warningLabel = new Label();
		warningLabel.setTextFill(Color.RED);
		HBox buttons = new HBox(20, btnCancel, btnSubmit);
		VBox rows = new VBox(20, new Label (message),new Label("Enter password to proceed: "), passwordField, buttons, warningLabel);
		StackPane pane = new StackPane(rows);
		pane.setPrefSize(460, 260);
		rows.setAlignment(Pos.CENTER);
		buttons.setAlignment(Pos.CENTER);
		
		//Load scene
		Stage passwordStage = new Stage();
		passwordStage.initModality(Modality.APPLICATION_MODAL);
		Scene scene = new Scene(pane);
		passwordStage.setTitle("Admin");
		passwordStage.setScene(scene);
		
		//Make buttons functional
		btnCancel.setOnAction((e) -> {
			myStore.setPasswordCorrect(false);
			passwordStage.close();
		});
		
		btnSubmit.setOnAction((e) -> {
			if(passwordField.getText().equals(myStore.getAdminPassword())) {
				System.out.println("Password is correct.");
				passwordStage.close();
				myStore.setPasswordCorrect(true);
			} else {
				System.out.println("Incorrect password.");
				warningLabel.setText("*Incorrect password");
				passwordField.setText("");
			}
		});
		
		passwordStage.showAndWait();
		return myStore.getPasswordCorrect();	
	}
	
	public void loadHomeScreen(Stage primaryStage) {
		//Define size of buttons
		final int BTN_WIDTH = 200;
		final int BTN_HEIGHT = 200;
		
		//Create root pane
		GridPane pane = new GridPane();
		pane.setPrefSize(SCREEN_WIDTH, SCREEN_HEIGHT);
		pane.setAlignment(Pos.CENTER);
		pane.setHgap(20);
		pane.setVgap(20);
		
		//Create menu buttons
		Button btnCheckout = new Button("Checkout");
		Button btnInventory = new Button("Inventory");
		Button btnCustomers = new Button("Customers");
		Button btnOrders = new Button("Orders");
		Button btnAdmin = new Button("Manager");
		Button btnReturns = new Button("Returns");
		
		//Set button size
		btnCheckout.setMinWidth(BTN_WIDTH);
		btnCheckout.setMinHeight(BTN_HEIGHT);
		btnInventory.setMinWidth(BTN_WIDTH);
		btnInventory.setMinHeight(BTN_HEIGHT);
		btnCustomers.setMinWidth(BTN_WIDTH);
		btnCustomers.setMinHeight(BTN_HEIGHT);
		btnOrders.setMinWidth(BTN_WIDTH);
		btnOrders.setMinHeight(BTN_HEIGHT);
		btnAdmin.setMinWidth(BTN_WIDTH);
		btnAdmin.setMinHeight(BTN_HEIGHT);
		btnReturns.setMinWidth(BTN_WIDTH);
		btnReturns.setMinHeight(BTN_HEIGHT);
		
		//Add buttons to grid-pane
		pane.add(btnInventory, 0, 0);
		pane.add(btnCustomers, 1, 0);
		pane.add(btnCheckout, 2, 0);
		pane.add(btnAdmin, 0, 1);
		pane.add(btnOrders, 1, 1);
		pane.add(btnReturns, 2, 1);
		
		//Make buttons functional
		btnInventory.setOnAction((e) -> {
			viewInventory(primaryStage);
		});
		
		btnCustomers.setOnAction((e) -> {
			loadCustomers(primaryStage);
		});
		
		btnCheckout.setOnAction((e) -> {
			loadCheckout(primaryStage);
		});
		
		btnAdmin.setOnAction((e) -> {
			managerControls(primaryStage);
		});
		
		//Load scene
		Scene scene = new Scene(pane);
		primaryStage.setTitle(myStore.getStoreName());
		primaryStage.setScene(scene);
		primaryStage.show();
	}
	
	public void viewInventory(Stage primaryStage) {
		//Create root pane and layout objects
		StackPane pane = new StackPane();
		pane.setPrefSize(SCREEN_WIDTH, SCREEN_HEIGHT);
		VBox rows = new VBox(20);
		HBox buttonHolder = new HBox(20);
		rows.setPadding(new Insets(20, 20, 20, 20));
		
		//Create buttons
		Button btnBack = new Button("Back");
		Button btnAdd = new Button("Add");
		Button btnEdit = new Button("Edit");
		Button btnDelete = new Button("Delete");
		
		//Format table
		TableView<SKU> table = new TableView<>();
		TableColumn<SKU, String> colID = new TableColumn<>("SKU Number");
		colID.setMinWidth(100);
		colID.setCellValueFactory(new PropertyValueFactory<SKU, String>("id"));
		TableColumn<SKU, String> colName = new TableColumn<>("Name");
		colName.setMinWidth(100);
		colName.setCellValueFactory(new PropertyValueFactory<SKU, String>("name"));
		TableColumn<SKU, String> colColors = new TableColumn<>("Colors");
		colColors.setMinWidth(100);
		colColors.setCellValueFactory(new PropertyValueFactory<SKU, String>("colors"));
		TableColumn<SKU, Double> colWholesale = new TableColumn<>("Wholesale Cost");
		colWholesale.setMinWidth(100);
		colWholesale.setCellValueFactory(new PropertyValueFactory<SKU, Double>("wholesaleCost"));
		TableColumn<SKU, Double> colRetail = new TableColumn<>("Retail Price");
		colRetail.setMinWidth(100);
		colRetail.setCellValueFactory(new PropertyValueFactory<SKU, Double>("retailPrice"));
		TableColumn<SKU, Integer> colShelfLife = new TableColumn<>("Shelf Life (Days)");
		colShelfLife.setMinWidth(100);
		colShelfLife.setCellValueFactory(new PropertyValueFactory<SKU, Integer>("shelfLifeInDays"));
		TableColumn<SKU, Integer> colStock = new TableColumn<>("Units in Stock");
		colStock.setMinWidth(100);
		colStock.setCellValueFactory(new PropertyValueFactory<SKU, Integer>("unitsInStock"));
		table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		
		//Add elements to layout
		rows.getChildren().add(table);
		pane.getChildren().add(rows);
		table.getColumns().addAll(colID, colName, colColors, colWholesale, colRetail, colShelfLife, colStock);
		rows.getChildren().add(buttonHolder);
		buttonHolder.getChildren().addAll(btnBack, btnAdd, btnEdit, btnDelete);
		btnEdit.setDisable(true);
		btnDelete.setDisable(true);
		
		
		//Load data to table
		inventoryData = FXCollections.observableArrayList(inventory);
		table.setItems(inventoryData);
		
		//Format text in cells
		colID.setCellFactory(tc -> new TableCell<SKU, String>() {
		    @Override
		    protected void updateItem(String id, boolean empty) {
		        super.updateItem(id, empty);
		        if (empty) {
		            setText(null);
		        } else {
		        	setText(id);
		        }
		    }
		});
		
		NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
		colWholesale.setCellFactory(tc -> new TableCell<SKU, Double>() {
		    @Override
		    protected void updateItem(Double price, boolean empty) {
		        super.updateItem(price, empty);
		        this.setAlignment(Pos.CENTER_RIGHT);
		        if (empty) {
		            setText(null);
		        } else {
		            setText(currencyFormat.format(price));
		        }
		    }
		});
		
		colRetail.setCellFactory(tc -> new TableCell<SKU, Double>() {
		    @Override
		    protected void updateItem(Double price, boolean empty) {
		        super.updateItem(price, empty);
		        this.setAlignment(Pos.CENTER_RIGHT);
		        if (empty) {
		            setText(null);
		        } else {
		            setText(currencyFormat.format(price));
		        }
		    }
		});
		
		colShelfLife.setCellFactory(tc -> new TableCell<SKU, Integer>() {
		    @Override
		    protected void updateItem(Integer days, boolean empty) {
		        super.updateItem(days, empty);
		        this.setAlignment(Pos.CENTER_RIGHT);
		        if (empty) {
		            setText(null);
		        } else {
		            setText(Integer.toString(days));
		        }
		    }
		});
		
		colStock.setCellFactory(tc -> new TableCell<SKU, Integer>() {
		    @Override
		    protected void updateItem(Integer days, boolean empty) {
		        super.updateItem(days, empty);
		        this.setAlignment(Pos.CENTER_RIGHT);
		        if (empty) {
		            setText(null);
		        } else {
		            setText(Integer.toString(days));
		        }
		    }
		});
		
		//Make buttons functional
		btnBack.setOnAction((e) -> {
			loadHomeScreen(primaryStage);		
		});
		
		btnAdd.setOnAction((e) -> {
			System.out.println(table.getSelectionModel().getSelectedItems());
			editInventory(primaryStage, null);
		});
		
		btnEdit.setOnAction((e) -> {
			System.out.println(table.getSelectionModel().getSelectedItem());
			SKU selectedSKU = (SKU)table.getSelectionModel().getSelectedItem();
			editInventory(primaryStage, selectedSKU);
		});
		
		btnDelete.setOnAction((e) -> {
			System.out.println(table.getSelectionModel().getSelectedItems());
			SKU selectedSKU = (SKU)table.getSelectionModel().getSelectedItem();
			String message = "Are you sure you want to delete " + selectedSKU.getName() + " from the inventory?";
			if(passwordVerificationWindow(message)) {
				System.out.println("True");
				inventory.remove(selectedSKU);
				inventoryData = FXCollections.observableArrayList(inventory);
				table.setItems(inventoryData);
			} else {
				System.out.println("This False");
			}
		});
		
		//Activate buttons on row selection
		ObservableList<SKU> selectedItems = table.getSelectionModel().getSelectedItems();
		selectedItems.addListener(new ListChangeListener<SKU>() {
			@Override
			public void onChanged(Change<? extends SKU> change) {
				btnEdit.setDisable(false);
				btnDelete.setDisable(false);
			}
		});
		
		//Load scene
		Scene scene = new Scene(pane);
		primaryStage.setTitle(myStore.getStoreName() + ": Inventory");
		primaryStage.setScene(scene);
		primaryStage.show();
	}
	
	public void editInventory(Stage primaryStage, SKU selectedSKU) {
		//Create layout elements
		StackPane pane = new StackPane();
		pane.setPrefSize(SCREEN_WIDTH, SCREEN_HEIGHT);
		VBox rows = new VBox();
		rows.setPadding(new Insets(20, 20, 20, 20));
		GridPane grid1 = new GridPane();
		grid1.setAlignment(Pos.CENTER);
		grid1.setHgap(10);
		grid1.setVgap(15);
		
		//Create Labels
		Label editSKU = new Label("Edit SKU");
		editSKU.setFont(Font.font("Arial", FontWeight.BOLD, 20));
				
		//Create text-fields and combo-box
		TextField tfID = new TextField();
		Label skuWarning = new Label();
		skuWarning.setTextFill(Color.RED);
		TextField tfName = new TextField();
		TextField tfWholesale = new TextField();
		TextField tfRetail = new TextField();
		TextField tfShelfLife = new TextField();
		ListView<String> lvColors = new ListView<>(color);
		TextField tfCurrentStock = new TextField();
		
		//Create buttons
		Button btnCancel = new Button("Cancel");
		Button btnSubmit = new Button("Submit");
		
		//Add elements to layout
		pane.getChildren().add(rows);
		rows.getChildren().add(grid1);
		grid1.add(editSKU, 0, 0);
		grid1.add(new Label("SKU Number:"), 0, 1);
		grid1.add(tfID, 1, 1);
		grid1.add(skuWarning, 2, 1);
		grid1.add(new Label("Product Name:"), 0, 2);
		grid1.add(tfName, 1, 2);
		grid1.add(new Label("Wholesale Cost:"), 0, 3);
		grid1.add(tfWholesale, 1, 3);
		grid1.add(new Label("Retail Price:"), 0, 4);
		grid1.add(tfRetail, 1, 4);
		grid1.add(new Label("Shelf Life:"), 0, 5);
		grid1.add(tfShelfLife, 1, 5);
		grid1.add(new Label("Color:"), 0, 6);
		grid1.add(lvColors, 1, 6);
		grid1.add(new Label("Units in Stock: "), 0, 7);
		grid1.add(tfCurrentStock, 1, 7);
		grid1.add(btnCancel, 0, 8);
		grid1.add(btnSubmit, 1, 8);
		
		//Add column constraints
		grid1.getColumnConstraints().addAll(new ColumnConstraints(100), new ColumnConstraints(160), new ColumnConstraints(80), new ColumnConstraints(160));
		grid1.setColumnSpan(skuWarning, 2);
		lvColors.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		
		//Load select SKU information
		if(selectedSKU != null) {
			tfID.setText(selectedSKU.getId());
			tfName.setText(selectedSKU.getName());
			tfWholesale.setText(Double.toString(selectedSKU.getWholesaleCost()));
			tfRetail.setText(Double.toString(selectedSKU.getRetailPrice()));
			tfShelfLife.setText(Integer.toString(selectedSKU.getShelfLifeInDays()));
			tfCurrentStock.setText(Integer.toString(selectedSKU.getUnitsInStock()));
			for (int i = 0; i < selectedSKU.getColors().size(); i++) {
				lvColors.getSelectionModel().select(selectedSKU.getColors().get(i));
			}
		}
		
		//Filters
		UnaryOperator<Change> idFilter = change -> {
			String text = change.getText();
			String fullText = change.getControlNewText();
			if(fullText.length() == 5) {
				for(int i = 0; i < inventory.size(); i++) {
					if(fullText.equals(inventory.get(i).getId()) && !fullText.equals(selectedSKU.getId())) {
						skuWarning.setText("*SKU #" + fullText + " already exists");
						return null;
					}
					skuWarning.setText("");
				}
			}
			if(text.matches("[0-9]*") && fullText.length() <= 5) {
				return change;
			} else {
				return null;
			}
		};
		TextFormatter<String> idFormatter = new TextFormatter<>(idFilter);
		tfID.setTextFormatter(idFormatter);
		
		UnaryOperator<Change> shelfLifeFilter = change -> {
			String text = change.getText();
			String fullText = change.getControlNewText();
			if(text.matches("[0-9]*") && fullText.length() <= 3) {
				return change;
			} else {
				return null;
			}
		};
		TextFormatter<String> shelfLifeFormatter = new TextFormatter<>(shelfLifeFilter);
		tfShelfLife.setTextFormatter(shelfLifeFormatter);
		
		UnaryOperator<Change> currentStockFilter = change -> {
			String text = change.getText();
			String fullText = change.getControlNewText();
			if(text.matches("[0-9]*") && fullText.length() <= 5) {
				return change;
			} else {
				return null;
			}
		};
		TextFormatter<String> currentStockFormatter = new TextFormatter<>(currentStockFilter);
		tfCurrentStock.setTextFormatter(currentStockFormatter);
		
		UnaryOperator<Change> currencyFilter = change -> {
			String text = change.getText();
			String fullText = change.getControlNewText();
			if(text.matches("[0-9.]*") && fullText.length() <= 7) {
				return change;
			} else {
				return null;
			}
		};
		TextFormatter<String> currencyFormatter = new TextFormatter<>(currencyFilter);
		tfWholesale.setTextFormatter(currencyFormatter);
		
		UnaryOperator<Change> retailFilter = change -> {
			String text = change.getText();
			String fullText = change.getControlNewText();
			if(text.matches("[0-9.]*") && fullText.length() <= 7) {
				return change;
			} else {
				return null;
			}
		};
		TextFormatter<String> retailFormatter = new TextFormatter<>(retailFilter);
		tfRetail.setTextFormatter(retailFormatter);
		
		//Make colors list functional
		lvColors.getSelectionModel().selectedItemProperty().addListener(ov -> {
			System.out.println(lvColors.getSelectionModel().getSelectedIndices());
			System.out.println(lvColors.getSelectionModel().getSelectedItems());
		});
		
		//Make buttons functional
		btnCancel.setOnAction((e) -> {
			viewInventory(primaryStage);
		});
				
		btnSubmit.setOnAction((e) -> {
			System.out.println(new Date().getTime());
			boolean abort = false;
			SKU sku = new SKU();
			if(tfID.getText().length() == 5) {
			} else {
				System.out.println("Invalid SKU number");
				abort = true;
			}
			if(tfName.getText().length() == 0) {
				System.out.println("Name field is empty");
				abort = true;
			};
			if(!abort) {
				sku.setId(tfID.getText());
				sku.setName(tfName.getText());
				sku.setWholesaleCost(Double.parseDouble(tfWholesale.getText()));
				sku.setRetailPrice(Double.parseDouble(tfRetail.getText()));
				sku.setShelfLifeInDays(Integer.parseInt(tfShelfLife.getText()));
				sku.setUnitsInStock(Integer.parseInt(tfCurrentStock.getText()));
				sku.setColors(lvColors.getSelectionModel().getSelectedItems());
				if(selectedSKU != null) {
					inventory.remove(selectedSKU);
				}
				inventory.add(sku);				
				viewInventory(primaryStage);
			}
		});
		
		//Load scene
		Scene scene = new Scene(pane);
		primaryStage.setTitle(myStore.getStoreName() + ": Edit Inventory");
		primaryStage.setScene(scene);
		primaryStage.show();
	}
	
	public void loadCheckout(Stage primaryStage) {
		//Create layout elements
		StackPane pane = new StackPane();
		pane.setPrefSize(SCREEN_WIDTH, SCREEN_HEIGHT);
		VBox rows = new VBox(20);
		rows.setPadding(new Insets(20, 20, 20, 20));
		GridPane grid1 = new GridPane();
		grid1.setAlignment(Pos.CENTER);
		grid1.setHgap(10);
		grid1.setVgap(15);
		GridPane grid2 = new GridPane();
		grid2.setAlignment(Pos.CENTER);
		grid2.setHgap(10);
		grid2.setVgap(15);
				
		//Create Labels
		Label customerInfo = new Label("Customer Information");
		customerInfo.setFont(Font.font("Arial", FontWeight.BOLD, 20));
						
		//Create text-fields and combo-box
		TextField tfPhoneSearch = new TextField();
				
		//Create buttons
		Button btnCancel = new Button("Cancel");
		Button btnSelect = new Button("Select Customer");
		btnSelect.setDisable(true);
		Button btnAdd = new Button("Add New Customer");
		Button btnSkip = new Button("Skip");
				
		//Add column constraints
		grid1.getColumnConstraints().addAll(new ColumnConstraints(120), new ColumnConstraints(140), new ColumnConstraints(80), new ColumnConstraints(160), new ColumnConstraints(230));
		grid1.setColumnSpan(customerInfo, 2);
		grid2.getColumnConstraints().addAll(new ColumnConstraints(120), new ColumnConstraints(140), new ColumnConstraints(80), new ColumnConstraints(160), new ColumnConstraints(230));
		grid2.setColumnSpan(btnSelect, 2);
		grid2.setColumnSpan(btnAdd, 2);
		
		//Update data
		ObservableList<Customer> searchData = FXCollections.observableArrayList(customers);
		ObservableList<Customer> tempData = FXCollections.observableArrayList();
				
		//Create table
		TableView<Customer> table = new TableView<>();
		TableColumn<Customer, String> colLastName = new TableColumn<>("Last Name");
		colLastName.setMinWidth(100);
		colLastName.setCellValueFactory(new PropertyValueFactory<Customer, String>("lastName"));
		TableColumn<Customer, String> colFirstName = new TableColumn<>("First Name");
		colFirstName.setMinWidth(100);
		colFirstName.setCellValueFactory(new PropertyValueFactory<Customer, String>("firstName"));
		TableColumn<Customer, String> colPhoneNumber = new TableColumn<>("Phone");
		colPhoneNumber.setMinWidth(110);
		colPhoneNumber.setCellValueFactory(new PropertyValueFactory<Customer, String>("phone"));
		TableColumn<Customer, String> colAddressFirstLine = new TableColumn<>("Street Address");
		colAddressFirstLine.setMinWidth(160);
		colAddressFirstLine.setCellValueFactory(new PropertyValueFactory<Customer, String>("addressFirstLine"));
		TableColumn<Customer, String> colCity = new TableColumn<>("City");
		colCity.setMinWidth(100);
		colCity.setCellValueFactory(new PropertyValueFactory<Customer, String>("city"));
		TableColumn<Customer, String> colEmail = new TableColumn<>("Email");
		colEmail.setMinWidth(160);
		colEmail.setCellValueFactory(new PropertyValueFactory<Customer, String>("email"));
		table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
				
		//Format text in cells
		colPhoneNumber.setCellFactory(tc -> new TableCell<Customer, String>() {
			@Override
			protected void updateItem(String phone, boolean empty) {
				super.updateItem(phone, empty);
				if (empty) {
					setText(null);
				} else {
					setText(String.format("(" + phone.substring(0,3) + ") " + phone.substring(3,6) + "-" + phone.substring(6)));
				}
			}
		});

		//Add elements to layout
		pane.getChildren().add(rows);
		rows.getChildren().add(grid1);
		grid1.add(customerInfo, 0, 0);
		grid1.add(new Label("Search Phone:"), 0, 1);
		grid1.add(tfPhoneSearch, 1, 1);
		rows.getChildren().add(table);
		table.getColumns().addAll(colLastName, colFirstName, colPhoneNumber, colAddressFirstLine, colCity, colEmail);
		table.setItems(searchData);
		table.setMaxHeight(200);
		rows.getChildren().add(grid2);
		grid2.add(btnSelect, 0, 0);
		grid2.add(btnAdd, 1, 0);
		grid2.add(btnCancel, 0, 1);
		grid2.add(btnSkip, 1, 1);
		
		//Filters
		UnaryOperator<Change> searchPhoneFilter = change -> {
			System.out.println("filter called");
			String text = change.getText();
			String fullText = change.getControlNewText();
			if(text.matches("[0-9]*") && fullText.length() <= 10) {
				return change;
			} else {
				return null;
			}
		};
		TextFormatter<String> searchPhoneFormatter = new TextFormatter<>(searchPhoneFilter);
		tfPhoneSearch.setTextFormatter(searchPhoneFormatter);
		
		tfPhoneSearch.setOnKeyReleased( e -> {
			String fullText = tfPhoneSearch.getText();
			tempData.clear();
			for (int i = 0; i < searchData.size(); i++) {
				if (searchData.get(i).getPhone().substring(0, fullText.length()).equals(fullText)) {
					tempData.add(searchData.get(i));
				}
				table.setItems(tempData);
			}
		});
		
		/*
		//Add TextFields and Labels to customer info GridPane
		pane.add(new Label("First Name:"), 0, 1);
		pane.add(tfFirstName, 1, 1);
		pane.add(new Label("Last Name:"), 2, 1);
		pane.add(tfLastName, 3, 1);
		pane.add(new Label("Phone:"), 0, 2);
		pane.add(tfPhoneNumber, 1, 2);
		pane.add(new Label("Email:"), 0, 3);
		pane.add(tfEmail, 1, 3);
		pane.add(new Label("Billing Address:"), 0, 4);
		pane.add(tfAddressFirstLine, 1, 4);
		pane.add(tfAddressSecondLine, 1, 5);
		pane.add(new Label("City:"), 0, 6);
		pane.add(tfCity, 1, 6);
		pane.add(new Label("State:"), 2, 6);
		pane.add(tfState, 3, 6);
		pane.add(new Label("Zipcode:"), 0, 7);
		pane.add(tfZipcode, 1, 7);
		pane.add(new Label("Credit Card:"), 0, 8);
		pane.add(tfCreditCard, 1, 8);
		
		//Add TextFields and Labels to reminders GridPane
		pane2.add(new Label("Date:"), 0, 0);
		pane2.add(tfDate, 1, 0);
		pane2.add(new Label("Occasion:"), 2, 0);
		pane2.add(cbOccasion, 3, 0);
		pane2.add(new Label("Recipient:"), 0, 1);
		pane2.add(tfRecipient, 1, 1);
		
		//Add Buttons to GridPane
		buttonsHolder.add(btnAddReminder, 0, 0);
		buttonsHolder.add(btnCancel, 0, 1);
		buttonsHolder.add(btnSubmit, 1, 1);
		
		//Add ColumnConstraints
		pane.getColumnConstraints().addAll(new ColumnConstraints(100), new ColumnConstraints(160), new ColumnConstraints(80), new ColumnConstraints(160));
		pane2.getColumnConstraints().addAll(new ColumnConstraints(100), new ColumnConstraints(160), new ColumnConstraints(80), new ColumnConstraints(160));
		buttonsHolder.getColumnConstraints().addAll(new ColumnConstraints(100), new ColumnConstraints(160), new ColumnConstraints(80), new ColumnConstraints(160));
		buttonsHolder.setColumnSpan(btnAddReminder, 2);
		pane.setColumnSpan(tfAddressFirstLine, 2);
		pane.setColumnSpan(tfAddressSecondLine, 2);
		pane.setColumnSpan(tfCreditCard, 2);
		
		//Add headers and grids to VBox
		main.getChildren().add(customerInfo);
		main.getChildren().add(pane);
		main.getChildren().add(remindersLb);
		main.getChildren().add(pane2);
		main.getChildren().add(buttonsHolder);
		scrollPane.setContent(main);
		
		//Load selected customer (set default values for text fields)
		if(existingCustomer != null) {
			tfFirstName.setText(existingCustomer.getFirstName());
			tfLastName.setText(existingCustomer.getLastName());
			tfPhoneNumber.setText(existingCustomer.getPhone());
			tfEmail.setText(existingCustomer.getEmail());
			tfAddressFirstLine.setText(existingCustomer.getAddressFirstLine());
			tfAddressSecondLine.setText(existingCustomer.getAddressSecondLine());
			tfCity.setText(existingCustomer.getCity());
			tfZipcode.setText(existingCustomer.getZipcode());
			tfCreditCard.setText(existingCustomer.getCreditCard());
		}
		*/
		
		
		//Activate buttons on row selection
		ObservableList<Customer> selectedItems = table.getSelectionModel().getSelectedItems();
		selectedItems.addListener(new ListChangeListener<Customer>() {
			@Override
			public void onChanged(Change<? extends Customer> change) {
				if(table.getSelectionModel().getSelectedItem() == null) {
					btnSelect.setDisable(true);
				} else {
					btnSelect.setDisable(false);
				}
				System.out.println(table.getSelectionModel().getSelectedItem());
			}
		});
		
		//Make buttons functional
		btnCancel.setOnAction((e) -> {
			loadHomeScreen(primaryStage);
		});
		
		btnSelect.setOnAction((e) -> {
			System.out.println("select pressed");
			Customer selectedCustomer = (Customer)table.getSelectionModel().getSelectedItem();
			loadCheckout2(primaryStage, selectedCustomer);
		});
		
		//Load Scene
		Scene scene = new Scene(pane);
		primaryStage.setTitle(myStore.getStoreName() + ": Checkout");
		primaryStage.setScene(scene);
		primaryStage.show();
	}
	
	public void loadCheckout2(Stage primaryStage, Customer customer) {
		//Create layout elements
		ScrollPane pane = new ScrollPane();
		pane.setPrefSize(SCREEN_WIDTH, SCREEN_HEIGHT);
		VBox rows = new VBox(20);
		VBox checkoutItems = new VBox(20);
		rows.setPadding(new Insets(20, 20, 20, 20));
		GridPane grid1 = new GridPane();
		grid1.setAlignment(Pos.CENTER);
		grid1.setHgap(10);
		grid1.setVgap(15);
		GridPane grid2 = new GridPane();
		grid2.setAlignment(Pos.CENTER);
		grid2.setHgap(10);
		grid2.setVgap(15);
				
		//Create Labels
		Label customerInfo = new Label("Customer Information");
		customerInfo.setFont(Font.font("Arial", FontWeight.BOLD, 20));
		Label customerName = new Label(customer.getLastName() + ", " + customer.getFirstName());
		Label customerAddress1 = new Label(customer.getAddressFirstLine());
		Label customerAddress2 = new Label(customer.getAddressSecondLine());
		Label customerCity = new Label(customer.getCity() + ", " + customer.getState() + " " + customer.getZipcode());
		Label allSKUs = new Label("All SKUs");
		allSKUs.setFont(Font.font("Arial", FontWeight.BOLD, 20));
		Label checkoutInfo = new Label("Items for Checkout");
		checkoutInfo.setFont(Font.font("Arial", FontWeight.BOLD, 20));
		
		//Create buttons
		Button btnCancel = new Button("Cancel");
		Button btnSelect = new Button("Select Item");
		btnSelect.setDisable(true);
		//Button btnAdd = new Button("Add New Customer");
		//Button btnSkip = new Button("Skip");
				
		//Add column constraints
		grid1.getColumnConstraints().addAll(new ColumnConstraints(120), new ColumnConstraints(140), new ColumnConstraints(80), new ColumnConstraints(160), new ColumnConstraints(230));
		grid1.setColumnSpan(customerInfo, 2);
		grid1.setColumnSpan(customerName, 2);
		grid1.setColumnSpan(customerAddress1, 2);
		grid1.setColumnSpan(customerAddress2, 2);
		grid1.setColumnSpan(customerCity, 2);
		grid1.setColumnSpan(checkoutInfo, 2);
		grid2.getColumnConstraints().addAll(new ColumnConstraints(120), new ColumnConstraints(140), new ColumnConstraints(80), new ColumnConstraints(160), new ColumnConstraints(230));
		
		
		//Format table
		TableView<SKU> table = new TableView<>();
		TableColumn<SKU, String> colID = new TableColumn<>("SKU Number");
		colID.setMinWidth(100);
		colID.setCellValueFactory(new PropertyValueFactory<SKU, String>("id"));
		TableColumn<SKU, String> colName = new TableColumn<>("Name");
		colName.setMinWidth(100);
		colName.setCellValueFactory(new PropertyValueFactory<SKU, String>("name"));
		TableColumn<SKU, String> colColors = new TableColumn<>("Colors");
		colColors.setMinWidth(100);
		colColors.setCellValueFactory(new PropertyValueFactory<SKU, String>("colors"));
		TableColumn<SKU, Double> colRetail = new TableColumn<>("Retail Price");
		colRetail.setMinWidth(100);
		colRetail.setCellValueFactory(new PropertyValueFactory<SKU, Double>("retailPrice"));
		TableColumn<SKU, Integer> colStock = new TableColumn<>("Units in Stock");
		colStock.setMinWidth(100);
		colStock.setCellValueFactory(new PropertyValueFactory<SKU, Integer>("unitsInStock"));
		table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		
		//Load data to table
		inventoryData = FXCollections.observableArrayList(inventory);
		table.setItems(inventoryData);
		
		//Format text in cells
		colID.setCellFactory(tc -> new TableCell<SKU, String>() {
		    @Override
		    protected void updateItem(String id, boolean empty) {
		        super.updateItem(id, empty);
		        if (empty) {
		            setText(null);
		        } else {
		        	setText(id);
		        }
		    }
		});
		
		NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
		colRetail.setCellFactory(tc -> new TableCell<SKU, Double>() {
		    @Override
		    protected void updateItem(Double price, boolean empty) {
		        super.updateItem(price, empty);
		        this.setAlignment(Pos.CENTER_RIGHT);
		        if (empty) {
		            setText(null);
		        } else {
		            setText(currencyFormat.format(price));
		        }
		    }
		});
		
		colStock.setCellFactory(tc -> new TableCell<SKU, Integer>() {
		    @Override
		    protected void updateItem(Integer days, boolean empty) {
		        super.updateItem(days, empty);
		        this.setAlignment(Pos.CENTER_RIGHT);
		        if (empty) {
		            setText(null);
		        } else {
		            setText(Integer.toString(days));
		        }
		    }
		});

		//Add elements to layout
		pane.setContent(rows);
		rows.getChildren().add(grid1);
		grid1.add(customerInfo, 0, 0);
		grid1.add(customerName, 0, 1);
		grid1.add(customerAddress1, 0, 2);
		grid1.add(customerAddress2, 0, 3);
		grid1.add(customerCity, 0, 3);
		grid1.add(allSKUs, 0, 4);
		rows.getChildren().add(table);
		table.getColumns().addAll(colID, colName, colColors, colRetail, colStock);
		table.setItems(inventoryData);
		table.setMaxHeight(200);
		rows.getChildren().add(grid2);
		grid2.add(btnSelect, 0, 0);
		grid2.add(checkoutInfo, 0, 1);
		rows.getChildren().add(checkoutItems);
		rows.getChildren().add(btnCancel);
		
		
		//Activate buttons on row selection
		ObservableList<SKU> selectedItems = table.getSelectionModel().getSelectedItems();
		selectedItems.addListener(new ListChangeListener<SKU>() {
			@Override
			public void onChanged(Change<? extends SKU> change) {
				if(table.getSelectionModel().getSelectedItem() == null) {
					btnSelect.setDisable(true);
				} else {
					btnSelect.setDisable(false);
				}
				System.out.println(table.getSelectionModel().getSelectedItem());
			}
		});
		
		//Make buttons functional
		btnCancel.setOnAction((e) -> {
			loadHomeScreen(primaryStage);
		});
		
		//Make buttons functional
		btnSelect.setOnAction((e) -> {
			SKU selectedSKU = (SKU)table.getSelectionModel().getSelectedItem();
			CheckOutItem item = new CheckOutItem(selectedSKU.getName(), selectedSKU.getColors().get(0), checkoutItems);
			item.setUpGrid();
			System.out.println(checkoutItems);
			checkoutItems.getChildren().add(item.grid);
		});
		
		//Load Scene
		Scene scene = new Scene(pane);
		primaryStage.setTitle(myStore.getStoreName() + ": Checkout");
		primaryStage.setScene(scene);
		primaryStage.show();
	}
	
	public void loadCustomers(Stage primaryStage) {
		//Create layout elements
		StackPane pane = new StackPane();
		pane.setPrefSize(SCREEN_WIDTH, SCREEN_HEIGHT);
		VBox rows = new VBox(20);
		HBox buttonHolder = new HBox(20);
		rows.setPadding(new Insets(20, 20, 20, 20));
		
		//Create buttons
		Button btnBack = new Button("Back");
		Button btnAdd = new Button("Add");
		Button btnEdit = new Button("Edit");
		Button btnDelete = new Button("Delete");
		
		//Update data
		data = FXCollections.observableArrayList(customers);
		
		//Create table
		TableView<Customer> table = new TableView<>();
		TableColumn<Customer, String> colLastName = new TableColumn<>("Last Name");
		colLastName.setMinWidth(100);
		colLastName.setCellValueFactory(new PropertyValueFactory<Customer, String>("lastName"));
		TableColumn<Customer, String> colFirstName = new TableColumn<>("First Name");
		colFirstName.setMinWidth(100);
		colFirstName.setCellValueFactory(new PropertyValueFactory<Customer, String>("firstName"));
		TableColumn<Customer, String> colPhoneNumber = new TableColumn<>("Phone");
		colPhoneNumber.setMinWidth(110);
		colPhoneNumber.setCellValueFactory(new PropertyValueFactory<Customer, String>("phone"));
		TableColumn<Customer, String> colAddressFirstLine = new TableColumn<>("Street Address");
		colAddressFirstLine.setMinWidth(160);
		colAddressFirstLine.setCellValueFactory(new PropertyValueFactory<Customer, String>("addressFirstLine"));
		TableColumn<Customer, String> colCity = new TableColumn<>("City");
		colCity.setMinWidth(100);
		colCity.setCellValueFactory(new PropertyValueFactory<Customer, String>("city"));
		TableColumn<Customer, String> colEmail = new TableColumn<>("Email");
		colEmail.setMinWidth(160);
		colEmail.setCellValueFactory(new PropertyValueFactory<Customer, String>("email"));
		table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		
		//Format text in cells
		colPhoneNumber.setCellFactory(tc -> new TableCell<Customer, String>() {
			@Override
			protected void updateItem(String phone, boolean empty) {
				super.updateItem(phone, empty);
				if (empty) {
					setText(null);
				} else {
					setText(String.format("(" + phone.substring(0,3) + ") " + phone.substring(3,6) + "-" + phone.substring(6)));
				}
			}
		});
		
		//Create layout
		rows.getChildren().add(table);
		pane.getChildren().add(rows);
		table.getColumns().addAll(colLastName, colFirstName, colPhoneNumber, colAddressFirstLine, colCity, colEmail);
		table.setItems(data);
		rows.getChildren().add(buttonHolder);
		buttonHolder.getChildren().addAll(btnBack, btnAdd, btnEdit, btnDelete);
		btnEdit.setDisable(true);
		btnDelete.setDisable(true);
		
		//Make buttons functional
		btnBack.setOnAction((e) -> {
			loadHomeScreen(primaryStage);		
		});
		
		btnEdit.setOnAction((e) -> {
			System.out.println(table.getSelectionModel().getSelectedItem());
			Customer selectedCustomer = (Customer)table.getSelectionModel().getSelectedItem();
			editCustomer(primaryStage, selectedCustomer);
		});
		
		btnAdd.setOnAction((e) -> {
			editCustomer(primaryStage, null);
		});
		
		btnDelete.setOnAction((e) -> {
			System.out.println(table.getSelectionModel().getSelectedItems());
			Customer selectedCustomer = (Customer)table.getSelectionModel().getSelectedItem();
			String message = "Are you sure you want to delete " + selectedCustomer.getFirstName() + " " + selectedCustomer.getLastName() + " from the inventory?";
			if(passwordVerificationWindow(message)) {
				System.out.println("True");
				customers.remove(selectedCustomer);
				data = FXCollections.observableArrayList(customers);
				table.setItems(data);
			} else {
				System.out.println("This False");
			}
		});
		
		//Activate buttons on row selection
		ObservableList<Customer> selectedItems = table.getSelectionModel().getSelectedItems();
		selectedItems.addListener(new ListChangeListener<Customer>() {
			@Override
			public void onChanged(Change<? extends Customer> change) {
				btnEdit.setDisable(false);
				btnDelete.setDisable(false);
			}
		});
		
		//Load scene
		Scene scene = new Scene(pane);
		primaryStage.setTitle(myStore.getStoreName() + ": Customers");
		primaryStage.setScene(scene);
		primaryStage.show();
	}
	
	public void editCustomer(Stage primaryStage, Customer existingCustomer) {
		System.out.println(existingCustomer);
		//Create layout elements
		ScrollPane scrollPane = new ScrollPane();
		VBox main = new VBox();
		main.setPrefSize(SCREEN_WIDTH, SCREEN_HEIGHT);
		main.setAlignment(Pos.CENTER);
		main.setSpacing(20);
		
		GridPane pane = new GridPane();
		pane.setAlignment(Pos.CENTER);
		pane.setHgap(10);
		pane.setVgap(15);
		
		GridPane pane2 = new GridPane();
		pane2.setAlignment(Pos.CENTER);
		pane2.setHgap(10);
		pane2.setVgap(15);
		
		GridPane buttonsHolder = new GridPane();
		buttonsHolder.setAlignment(Pos.CENTER);
		buttonsHolder.setHgap(10);
		buttonsHolder.setVgap(15);
		
		//Create Labels
		Label customerInfo = new Label("Customer Information");
		customerInfo.setFont(Font.font("Arial", FontWeight.BOLD, 20));
		Label remindersLb = new Label("Reminders");
		remindersLb.setFont(Font.font("Arial", FontWeight.BOLD, 20));
		
		//Create TextFields
		TextField tfFirstName = new TextField();
		TextField tfLastName = new TextField();
		TextField tfPhoneNumber = new TextField();
		TextField tfEmail = new TextField();
		TextField tfAddressFirstLine = new TextField();
		TextField tfAddressSecondLine = new TextField();
		TextField tfCity = new TextField();
		TextField tfState = new TextField();
		TextField tfZipcode = new TextField();
		TextField tfCreditCard = new PasswordField();
		TextField tfRecipient = new TextField();
		TextField tfDate = new TextField();
		ComboBox cbOccasion = new ComboBox();
		
		//Create Buttons
		Button btnAddReminder = new Button("Add a Reminder");
		Button btnCancel = new Button("Cancel");
		Button btnSubmit = new Button("Submit");
		
		//Add TextFields and Labels to customer info GridPane
		pane.add(new Label("First Name:"), 0, 1);
		pane.add(tfFirstName, 1, 1);
		pane.add(new Label("Last Name:"), 2, 1);
		pane.add(tfLastName, 3, 1);
		pane.add(new Label("Phone:"), 0, 2);
		pane.add(tfPhoneNumber, 1, 2);
		pane.add(new Label("Email:"), 0, 3);
		pane.add(tfEmail, 1, 3);
		pane.add(new Label("Billing Address:"), 0, 4);
		pane.add(tfAddressFirstLine, 1, 4);
		pane.add(tfAddressSecondLine, 1, 5);
		pane.add(new Label("City:"), 0, 6);
		pane.add(tfCity, 1, 6);
		pane.add(new Label("State:"), 2, 6);
		pane.add(tfState, 3, 6);
		pane.add(new Label("Zipcode:"), 0, 7);
		pane.add(tfZipcode, 1, 7);
		pane.add(new Label("Credit Card:"), 0, 8);
		pane.add(tfCreditCard, 1, 8);
		
		//Add TextFields and Labels to reminders GridPane
		pane2.add(new Label("Date:"), 0, 0);
		pane2.add(tfDate, 1, 0);
		pane2.add(new Label("Occasion:"), 2, 0);
		pane2.add(cbOccasion, 3, 0);
		pane2.add(new Label("Recipient:"), 0, 1);
		pane2.add(tfRecipient, 1, 1);
		
		//Add Buttons to GridPane
		buttonsHolder.add(btnAddReminder, 0, 0);
		buttonsHolder.add(btnCancel, 0, 1);
		buttonsHolder.add(btnSubmit, 1, 1);
		
		//Add ColumnConstraints
		pane.getColumnConstraints().addAll(new ColumnConstraints(100), new ColumnConstraints(160), new ColumnConstraints(80), new ColumnConstraints(160));
		pane2.getColumnConstraints().addAll(new ColumnConstraints(100), new ColumnConstraints(160), new ColumnConstraints(80), new ColumnConstraints(160));
		buttonsHolder.getColumnConstraints().addAll(new ColumnConstraints(100), new ColumnConstraints(160), new ColumnConstraints(80), new ColumnConstraints(160));
		buttonsHolder.setColumnSpan(btnAddReminder, 2);
		pane.setColumnSpan(tfAddressFirstLine, 2);
		pane.setColumnSpan(tfAddressSecondLine, 2);
		pane.setColumnSpan(tfCreditCard, 2);
		
		//Add headers and grids to VBox
		main.getChildren().add(customerInfo);
		main.getChildren().add(pane);
		main.getChildren().add(remindersLb);
		main.getChildren().add(pane2);
		main.getChildren().add(buttonsHolder);
		scrollPane.setContent(main);
		
		//Load selected customer (set default values for text fields)
		if(existingCustomer != null) {
			tfFirstName.setText(existingCustomer.getFirstName());
			tfLastName.setText(existingCustomer.getLastName());
			tfPhoneNumber.setText(existingCustomer.getPhone());
			tfEmail.setText(existingCustomer.getEmail());
			tfAddressFirstLine.setText(existingCustomer.getAddressFirstLine());
			tfAddressSecondLine.setText(existingCustomer.getAddressSecondLine());
			tfCity.setText(existingCustomer.getCity());
			tfZipcode.setText(existingCustomer.getZipcode());
			tfCreditCard.setText(existingCustomer.getCreditCard());
		}
		
		//Filters
		UnaryOperator<Change> phoneFilter = change -> {
			String text = change.getText();
			String fullText = change.getControlNewText();
			if(text.matches("[0-9]*") && fullText.length() <= 10) {
				return change;
			}
			return null;
		};
		TextFormatter<String> phoneFormatter = new TextFormatter<>(phoneFilter);
		tfPhoneNumber.setTextFormatter(phoneFormatter);
		
		UnaryOperator<Change> zipcodeFilter = change -> {
			String text = change.getText();
			String fullText = change.getControlNewText();
			if(text.matches("[0-9]*") && fullText.length() <= 5) {
				return change;
			}
			return null;
		};
		TextFormatter<String> zipcodeFormatter = new TextFormatter<>(zipcodeFilter);
		tfZipcode.setTextFormatter(zipcodeFormatter);
		
		UnaryOperator<Change> creditCardFilter = change -> {
			String text = change.getText();
			String fullText = change.getControlNewText();
			if(text.matches("[0-9]*") && fullText.length() <= 16) {
				return change;
			}
			return null;
		};
		TextFormatter<String> creditCardFormatter = new TextFormatter<>(creditCardFilter);
		tfCreditCard.setTextFormatter(creditCardFormatter);
		
		//Make buttons functional
		btnCancel.setOnAction((e) -> {
			loadCustomers(primaryStage);
		});
		
		btnSubmit.setOnAction((e) -> {
			Customer customer = new Customer();
			if(existingCustomer == null) {
				customer.setID(myStore.getNextCustomerID());
				myStore.advanceCustomerID();
			} else {
				customer.setID(existingCustomer.getID());
			}
			customer.setFirstName(tfFirstName.getText());
			customer.setLastName(tfLastName.getText());
			customer.setPhone(tfPhoneNumber.getText());
			if(tfEmail.getText().matches("^(.+)@(.+)$")) {
				customer.setEmail(tfEmail.getText());
			} else {
				System.out.println("Invalid email");
			}
			customer.setAddressFirstLine(tfAddressFirstLine.getText());
			customer.setAddressSecondLine(tfAddressSecondLine.getText());
			customer.setCity(tfCity.getText());
			customer.setState(tfState.getText());
			customer.setZipcode(tfZipcode.getText());
			customer.setCreditCard(tfCreditCard.getText());
			if(existingCustomer != null) {
				customers.remove(existingCustomer);
			}
			customers.add(customer);
			loadCustomers(primaryStage);
		});
		
		Scene scene = new Scene(scrollPane);
		primaryStage.setTitle(myStore.getStoreName() + ": Edit Customer");
		primaryStage.setScene(scene);
		primaryStage.show();
	}
	
	public void managerControls(Stage primaryStage) {
		//Create layout elements
		ScrollPane scrollPane = new ScrollPane();
		VBox main = new VBox();
		main.setPrefSize(SCREEN_WIDTH, SCREEN_HEIGHT);
		main.setAlignment(Pos.CENTER);
		main.setSpacing(20);
		
		GridPane pane = new GridPane();
		pane.setAlignment(Pos.CENTER);
		pane.setHgap(10);
		pane.setVgap(15);
		
		GridPane buttonsHolder = new GridPane();
		buttonsHolder.setAlignment(Pos.CENTER);
		buttonsHolder.setHgap(10);
		buttonsHolder.setVgap(15);
		
		GridPane pane2 = new GridPane();
		pane2.setAlignment(Pos.CENTER);
		pane2.setHgap(10);
		pane2.setVgap(15);
		
		GridPane buttonsHolder2 = new GridPane();
		buttonsHolder2.setAlignment(Pos.CENTER);
		buttonsHolder2.setHgap(10);
		buttonsHolder2.setVgap(15);
		
		//Create Labels
		Label storeInfo = new Label("Store Information");
		storeInfo.setFont(Font.font("Arial", FontWeight.BOLD, 20));
		Label adminInfo = new Label("Admin Information");
		adminInfo.setFont(Font.font("Arial", FontWeight.BOLD, 20));
		
		//Create Buttons
		Button btnBack = new Button("Back");
		Button btnEdit = new Button("Edit");
		Button btnEditAdmin = new Button("Edit ID#s");
		
		//Display store information
		pane.add(new Label(myStore.getStoreName()), 0, 0);
		pane.add(new Label(myStore.getAddressFirstLine()), 0, 1);
		pane.add(new Label(myStore.getAddressSecondLine()), 0, 2);
		pane.add(new Label(myStore.getCity() + ", " + myStore.getState() + " " + myStore.getZipcode()), 0, 3);
		pane.add(new Label(myStore.getPhone()), 0, 4);
		pane.add(new Label(myStore.getEmail()), 0, 5);
		pane.add(new Label(myStore.getWebsite()), 0, 6);
		
		//Display SKU, customer, and employee ID value
		pane2.add(new Label("Next SKU Number:"), 0, 0);
		pane2.add(new Label(Integer.toString(myStore.getNextSKUNumber())), 1, 0);
		pane2.add(new Label("Next Customer ID:"), 0, 1);
		pane2.add(new Label(Integer.toString(myStore.getNextCustomerID())), 1, 1);
		pane2.add(new Label("Next Employee ID:"), 0, 2);
		pane2.add(new Label(Integer.toString(myStore.getNextEmployeeID())), 1, 2);
		pane2.add(new Label("Next Incident ID:"), 0, 3);
		pane2.add(new Label(Integer.toString(myStore.getNextIncidentID())), 1, 3);
		
		//Add Buttons to GridPane
		buttonsHolder.add(btnBack, 0, 0);
		buttonsHolder.add(btnEdit, 1, 0);
		buttonsHolder2.add(btnEditAdmin, 0, 0);
		
		//Add ColumnConstraints
		pane.getColumnConstraints().addAll(new ColumnConstraints(300), new ColumnConstraints(60), new ColumnConstraints(80), new ColumnConstraints(60));
		buttonsHolder.getColumnConstraints().addAll(new ColumnConstraints(60), new ColumnConstraints(200), new ColumnConstraints(80), new ColumnConstraints(160));
		pane2.getColumnConstraints().addAll(new ColumnConstraints(200), new ColumnConstraints(160), new ColumnConstraints(80), new ColumnConstraints(60));
		buttonsHolder2.getColumnConstraints().addAll(new ColumnConstraints(160), new ColumnConstraints(100), new ColumnConstraints(80), new ColumnConstraints(160));
		
		//Add headers and grids to VBox
		main.getChildren().add(storeInfo);
		main.getChildren().add(pane);
		main.getChildren().add(buttonsHolder);
		main.getChildren().add(adminInfo);
		main.getChildren().add(pane2);
		main.getChildren().add(buttonsHolder2);
		scrollPane.setContent(main);
		
		//Make buttons functional
		btnBack.setOnAction((e) -> {
			loadHomeScreen(primaryStage);
		});
		
		btnEdit.setOnAction((e) -> {
			if(passwordVerificationWindow("Password required to edit store information.")) {
				editStoreSettings(primaryStage);
			}
		});
		
		Scene scene = new Scene(scrollPane);
		primaryStage.setTitle(myStore.getStoreName() + ": Manager Controls");
		primaryStage.setScene(scene);
		primaryStage.show();
	}
	
	public void editStoreSettings(Stage primaryStage) {
		//Create layout elements
		ScrollPane scrollPane = new ScrollPane();
		VBox main = new VBox();
		main.setPrefSize(SCREEN_WIDTH, SCREEN_HEIGHT);
		main.setAlignment(Pos.CENTER);
		main.setSpacing(20);
		
		GridPane pane = new GridPane();
		pane.setAlignment(Pos.CENTER);
		pane.setHgap(10);
		pane.setVgap(15);
		
		GridPane buttonsHolder = new GridPane();
		buttonsHolder.setAlignment(Pos.CENTER);
		buttonsHolder.setHgap(10);
		buttonsHolder.setVgap(15);
		
		//Create Labels
		Label storeInfo = new Label("Store Information");
		storeInfo.setFont(Font.font("Arial", FontWeight.BOLD, 20));
		
		//Create TextFields
		TextField tfName = new TextField();
		TextField tfAddressFirstLine = new TextField();
		TextField tfAddressSecondLine = new TextField();
		TextField tfCity = new TextField();
		TextField tfState = new TextField();
		TextField tfZipcode = new TextField();
		TextField tfPhoneNumber = new TextField();
		TextField tfEmail = new TextField();
		TextField tfWebsite = new TextField();
		
		//Create Buttons
		Button btnCancel = new Button("Cancel");
		Button btnSubmit = new Button("Submit");
		
		//Add TextFields and Labels to customer info GridPane
		pane.add(new Label("Store Name:"), 0, 0);
		pane.add(tfName, 1, 0);
		pane.add(new Label("Store Address:"), 0, 1);
		pane.add(tfAddressFirstLine, 1, 1);
		pane.add(tfAddressSecondLine, 1, 2);
		pane.add(new Label("City:"), 0, 3);
		pane.add(tfCity, 1, 3);
		pane.add(new Label("State:"), 2, 3);
		pane.add(tfState, 3, 3);
		pane.add(new Label("Zipcode:"), 0, 4);
		pane.add(tfZipcode, 1, 4);
		pane.add(new Label("Phone:"), 0, 5);
		pane.add(tfPhoneNumber, 1, 5);
		pane.add(new Label("Email:"), 0, 6);
		pane.add(tfEmail, 1, 6);
		pane.add(new Label("Website:"), 0, 7);
		pane.add(tfWebsite, 1, 7);

		
		//Add Buttons to GridPane
		buttonsHolder.add(btnCancel, 0, 0);
		buttonsHolder.add(btnSubmit, 1, 0);
		
		//Add ColumnConstraints
		pane.getColumnConstraints().addAll(new ColumnConstraints(100), new ColumnConstraints(160), new ColumnConstraints(80), new ColumnConstraints(160));
		buttonsHolder.getColumnConstraints().addAll(new ColumnConstraints(100), new ColumnConstraints(160), new ColumnConstraints(80), new ColumnConstraints(160));
		pane.setColumnSpan(tfAddressFirstLine, 2);
		pane.setColumnSpan(tfAddressSecondLine, 2);
		
		//Add headers and grids to VBox
		main.getChildren().add(storeInfo);
		main.getChildren().add(pane);
		main.getChildren().add(buttonsHolder);
		scrollPane.setContent(main);
		
		//Load store information (set default values for text fields)
		tfName.setText(myStore.getStoreName());
		tfAddressFirstLine.setText(myStore.getAddressFirstLine());
		tfAddressSecondLine.setText(myStore.getAddressSecondLine());
		tfCity.setText(myStore.getCity());
		tfState.setText(myStore.getState());
		tfZipcode.setText(myStore.getZipcode());
		tfPhoneNumber.setText(myStore.getPhone());
		tfEmail.setText(myStore.getEmail());
		tfWebsite.setText(myStore.getWebsite());
		
		//Filters
		UnaryOperator<Change> phoneFilter = change -> {
			String text = change.getText();
			String fullText = change.getControlNewText();
			if(text.matches("[0-9]*") && fullText.length() <= 10) {
				return change;
			}
			return null;
		};
		TextFormatter<String> phoneFormatter = new TextFormatter<>(phoneFilter);
		tfPhoneNumber.setTextFormatter(phoneFormatter);
		
		UnaryOperator<Change> zipcodeFilter = change -> {
			String text = change.getText();
			String fullText = change.getControlNewText();
			if(text.matches("[0-9]*") && fullText.length() <= 5) {
				return change;
			}
			return null;
		};
		TextFormatter<String> zipcodeFormatter = new TextFormatter<>(zipcodeFilter);
		tfZipcode.setTextFormatter(zipcodeFormatter);
		
		//Make buttons functional
		btnCancel.setOnAction((e) -> {
			managerControls(primaryStage);
		});
		
		btnSubmit.setOnAction((e) -> {
			myStore.setStoreName(tfName.getText());
			myStore.setAddressFirstLine(tfAddressFirstLine.getText());
			myStore.setAddressSecondLine(tfAddressSecondLine.getText());
			myStore.setCity(tfCity.getText());
			myStore.setState(tfState.getText());
			myStore.setZipcode(tfZipcode.getText());
			myStore.setPhone(tfPhoneNumber.getText());
			myStore.setWebsite(tfWebsite.getText());
			myStore.setEmail(tfEmail.getText());
			managerControls(primaryStage);
		});
		
		Scene scene = new Scene(scrollPane);
		primaryStage.setTitle(myStore.getStoreName() + ": Edit Store Settings");
		primaryStage.setScene(scene);
		primaryStage.show();

	}

	public void exportCustomersToDataBase(ArrayList<Customer> customerList) throws IOException {
		try (ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream("CustomerDataBase.dat"));) {
			output.writeObject(customerList);
		} catch (IOException e) {
			System.out.println(e);
		}
	}
	
	public ArrayList<Customer> inputCustomersFromDataBase() throws ClassNotFoundException, IOException, FileNotFoundException {
		ArrayList<Customer> customerList = new ArrayList<>();
		try (ObjectInputStream input = new ObjectInputStream(new FileInputStream("CustomerDataBase.dat"));) {
			customerList.addAll((ArrayList<Customer>)(input.readObject()));
		}  catch (Exception e) {
			System.out.println(e);
			return null;
		} 
		return customerList;
	}
	
	public void exportInventoryToFile(ArrayList<SKU> skuList) throws IOException {
		try (ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream("Inventory.dat"));) {
			output.writeObject(skuList);
		} catch (IOException e) {
			System.out.println(e);
		}
	}
	
	public ArrayList<SKU> readInventoryFromFile() throws ClassNotFoundException, IOException, FileNotFoundException {
		ArrayList<SKU> skuList = new ArrayList<>();
		try (ObjectInputStream input = new ObjectInputStream(new FileInputStream("Inventory.dat"));) {
			skuList.addAll((ArrayList<SKU>)(input.readObject()));
		}  catch (Exception e) {
			System.out.println(e);
			return null;
		} 
		return skuList;
	}
}

class CheckOutItem {
	
	GridPane grid = new GridPane();
	Label itemName = new Label();
	Label itemColor = new Label();
	Label itemQuantity = new Label("Quantity:");
	TextField tfQuantity = new TextField();
	Button btnRemove = new Button("Remove");
	
	public CheckOutItem() {
		
	}
	
	public CheckOutItem(String name, String color, VBox container) {
		itemName.setText(name);
		itemColor.setText(color);
		
		btnRemove.setOnAction((e) -> {
			System.out.println("pressed");
			System.out.println(this);
			System.out.println(container);
			container.getChildren().remove(this.grid);
		});
	}
	
	public void setUpGrid() {
		grid.add(itemName, 0, 0);
		grid.add(itemColor, 1, 0);
		grid.add(itemQuantity, 2, 0);
		grid.add(tfQuantity, 3, 0);
		grid.add(btnRemove, 4, 0);
		grid.setAlignment(Pos.CENTER);
		grid.setHgap(10);
		grid.setVgap(15);
		grid.getColumnConstraints().addAll(new ColumnConstraints(120), new ColumnConstraints(140), new ColumnConstraints(80), new ColumnConstraints(160), new ColumnConstraints(230));
	}
	
	
	
}