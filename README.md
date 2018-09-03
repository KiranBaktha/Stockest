# Stockest
An android app that helps you keep track of stock prices





This is the Intial starting Fragment that is displayed where you can type in the name of the company whose stock price you wish to monitor. Stock symbols and company names are fetched from IEXtrading API.

<img src="https://github.com/KiranBaktha/Stockest/blob/master/Screenshots/Start_Fragment.PNG" width="200" height="400">

Let's say Apple is selected. Next, an Info fragment is loaded that contains an interactive web view displaying a price graph from Yahoo Finance. From this fragment user can either add to watchlist or ask the app to summarize the stock.

<img src="https://github.com/KiranBaktha/Stockest/blob/master/Screenshots/Stock_Info_Fragment.PNG" width="200" height="400">

<p align="justify">
If summarize button is selected, another fragment containing the stock quotes is displayed in a list view. The quotes are fetched from IEX Trading API and the app also computes the previous week's average stock change percent using a simple averaging function.
</p>

<img src="https://github.com/KiranBaktha/Stockest/blob/master/Screenshots/Summarize_Fragment.PNG" width="200" height="400">

If the user wishes to add to watchlist, a dialog box requesting the target price to watch is displayed.


<img src="https://github.com/KiranBaktha/Stockest/blob/master/Screenshots/Setting_Target.PNG" width="200" height="400">

<p align="justify">
Watchlist is a seperate activity containing the watchlisted stocks in a listview. If the target price for any of the stock has reached, it is highlighted in red. The user can refresh the list manually using the menu bar or set periodic refresh in which case, every 30 minutes the stocks in the watchlist are refreshed to check their latest price (using Android Alarm Manager and Wake Locks). If any of the stocks reach their target price, a notification message is displayed on the device. 
</p>

<img src="https://github.com/KiranBaktha/Stockest/blob/master/Screenshots/Watch_List.PNG" width="200" height="400">



