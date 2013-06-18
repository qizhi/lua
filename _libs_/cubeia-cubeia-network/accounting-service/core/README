ACCOUNTING CORE
===============

This project contains all core accounting classes including entities, daos and
managers. It does not implement any service interface and is dependent on the API
only for utilities. 

This project is Spring wired:

    accounting-app.xml
    
Some Spring beans to keep track of:

    "accounting.dataSource" - Main data source, uses "java:comp/env/jdbc/accountingDS"
    by defualt but should be overriden by test cases.
    
    "accounting.transactionManager" - Main transaction manager.
    
    "accounting.accountingManager" - AccountingManager implementation.

