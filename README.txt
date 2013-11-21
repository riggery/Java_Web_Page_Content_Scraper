Some Deteil:

1.redirectPageSimple(Document doc, String keywords, int number)

By observing, I found in a page direct link, like
<li> <a href="?tab_value=all&amp;search_query=yamaha		+motor&amp;search_constraint=0&amp;Find=Find&amp;ss=false&amp;ic=16_16" 
onclick="setComparisonItem(this.href , '',true);return false;">2</a></li>

Attribute "ic" is the key to direct page. Ex. direct to page 1, 0<=ic<16
direct to page 2, 16<=ic<32



2. redirectPage(Document doc, String keywords, int number) 

I also wrote another redirectPage method which utilize DOM operation.
It depends on page parsing, so not so stable.



3. Jar file generation

The method I used is combine external library content with my code.