Feature:Client
Background:
	Given I navigate to mifos
	And I use login folder 
	When I login into mifos site using excel sheet
			| Login.xlsx  | 
	Then I should see logged in successfully

@clientcreationg		
Scenario Outline: As User creates the clients using excelsheet
	
	Given I setup the clients 
		| Clientnavigation.xlsx |
	When I entered the values into client form using
		 |<Createclient>|
	Then I should see client created successfully
	     |<Createclient>|
	When I set up the new create loan
	     |<Newcreateloan>|
	Then I verified the "Summary" details successfully	
		|<Newcreateloan>|
	And I verified the "Repayment Schedule" details successfully	
		|<Newcreateloan>|

Examples:
 | Createclient       | Newcreateloan       |
 | Createclient.xlsx  | Newcreateloan.xlsx  |
 | Createclient1.xlsx | Newcreateloan1.xlsx |
 | Createclient2.xlsx | Newcreateloan2.xlsx |


@clientcreationg		
Scenario: As User creates the different loans to single user
	
	Given I setup the clients 
		| Clientnavigation.xlsx |
	When I entered the values into client form using
		 |Createclient.xlsx|
	Then I should see client created successfully
	     |Createclient.xlsx|
	When I set up the new create loan
	     |Newcreateloan.xlsx|
	Then I verified the "Summary" details successfully	
		|Newcreateloan.xlsx|
    
    When I went back to the client
    And I set up the new create loan
	    |Newcreateloan.xlsx|
	Then I verified the "Summary" details successfully	
		|Newcreateloan.xlsx|
    
@clientcreation		
Scenario: As User creates the loans,make repayment and verifies the tabs 

    Given I setup the clients 
		| Clientnavigation.xlsx |
	When I entered the values into client form using
	 |Createclient.xlsx|
	Then I should see client created successfully
	     |Createclient.xlsx|
	     
	When I set up the new create loan
	     |Newcreateloan.xlsx|
	And I disburse the new created loan
		 | Disburse.xlsx  |
    Then I verified the "Summary" details successfully	
		|Newcreateloan.xlsx|
    And I verified the "Repayment Schedule" details successfully	
		|Newcreateloan.xlsx|
    
    When I make repayment and verified the following tabs
       |Makerepayment1.xlsx|Summary|Repayment Schedule|Transactions|
       |Makerepayment2.xlsx|Summary|Repayment Schedule|Transactions|

     And I disburse the new created loan
		 | Disburse1.xlsx  |
    