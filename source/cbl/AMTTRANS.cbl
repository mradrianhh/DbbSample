       Identification Division.
       Program-Id. Amttrans.
       Author. Adrian Hardy.

       Environment Division.
       Configuration Section.
       Special-Names.
           Decimal-Point Is Comma.

       Data Division.

       Local-Storage Section.

       01 Flags.
          05 No-Trans-Fee-Flag  Pic 9 Value 0.
             88 No-Trans-Fee          Value 1.
             88 Trans-Fee             Value 0.

       Linkage Section.

       Copy Account.

       Copy Amttrans.

       Procedure Division Using Account Amount-Transaction.

           Perform Eval-Account-Type
           Perform Eval-Transaction-Class
           Perform Eval-Transaction-Type

           Goback.

       Eval-Account-Type Section.

           Evaluate Account-Type
           When 'sparbsu'
             If (Account-Bsu-Savings - Transaction-Amount) < 0
               Perform Discard-Transaction
             End-If
           When Other
             Perform Discard-Transaction
           End-Evaluate

           Exit.

       Eval-Transaction-Class Section.

           Evaluate Transaction-Class
           When 'A'
           When '9'
             Continue
           When Other
             Perform Discard-Transaction
           End-Evaluate

           Exit.

       Eval-Transaction-Type Section.

           Evaluate Transaction-Type
           When 1
             Set No-Trans-Fee To True
             Perform Process-Transaction
           When Other
             Perform Discard-Transaction
           End-Evaluate

           Exit.

       Process-Transaction Section.

           Compute Account-Balance = Account-Balance
             - Transaction-Amount

           Exit.

       Discard-Transaction Section.

           Display 'Discarding transaction. . .'

           Exit.
