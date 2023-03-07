       Identification Division.
       Program-Id. Main.
       Author. Adrian Hardy.

       Environment Division.
       Configuration Section.
       Special-Names.
           Decimal-Point Is Comma.

       Data Division.

       Working-Storage Section.

       01 Ws-Pgms.
          05 Pgm-Amttrans  Pic X(8) Value 'AMTTRANS'.

       Copy Account.

       Copy Amttrans.

       Procedure Division.

           Perform 0000-Init
           Perform 1000-Main
           Perform 2000-Exit

           Goback.

       0000-Init Section.

           Exit.

       1000-Main Section.

           Perform Init-Account
           Perform Process-Transaction

           Exit.

       2000-Exit Section.

           Exit.

       Init-Account Section.

           Initialize Account
           Move 1000000 To Account-Balance

           Exit.

       Process-Transaction Section.

           Perform Get-Next-Transaction

           Evaluate Transaction-Code
           When 16
             Call Pgm-Amttrans Using Account Amount-Transaction
           When Other
             Continue
           End-Evaluate

           Exit.

       Get-Next-Transaction Section.

           Initialize Amount-Transaction.
           Move 16 To Transaction-Code
           Move 'A' To Transaction-Class
           Move '1' To Transaction-Type
           Move 10000 To Transaction-Amount

           Exit.