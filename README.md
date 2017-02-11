# Extract-Valid-wikilinks-and-Generate-Adjacency-Graph

we will use the wikilinks data set downloadable here:
https://s3.amazonaws.com/ufidsdata/
enwikilatestpagesarticles.
xml
You can truncate this file to smaller size for debugging.
In this lab, your job is to generate an adjacency graph from the dataset provided.
1. Write a MapReduce job that extracts wikilinks and also remove all the red links . The
dataset contains, for each article, a list of links going out of that article, as well as the
articles title. The input for this job should be the Wikipedia dataset above, and the job
should look at the XML text for each article, extract title and wikilinks. A single page
would be represented as follows (with some fields and attributes omitted):
<page> <title>Title</title> ...
<text ...>
Body of page
[[link text]]
</text>
</page>
Your task is to extract the Title and the wikilinks from these articles. Within the text of the
page, a wikilink is indicated by [[link text]]. In the simplest kind of link, the link text is the
name of another page (with any spaces replaced with underscores). A more complicated
kind gives different text to appear as well as the name of the page: [[page name|text to
appear]]. How to differentiate wikilinks to other types of links? Please refer to the
http://en.Wikipedia.org/wiki/Help:Wikilinks#Wikilinks .
There two formats of wikilinks as described in the above Wikipedia page. And repeating
here:
A. [[abc]] is seen as “abc” in text and links to page “abc”. We need to extract “abc” out.
B. [[a|b]] is labelled “b” on this page but links to page “a”. We need to extract “a” out.
But “abc” in (A) and “a” in (B) may be a red link which basically means there is no page
with title “abc” and “a” in the wiki dataset. You need to exclude all red links in this job.
Note: we need to replace all the empty space ‘ ’ in the Title and links(title of other pages)
with an underline ‘_’. No other processing is needed in this project.
2. Write another MapReduce job to generate the Adjacency graph, i.e a graph with the
format:
page_title1 link1 link2 …
page_titile2 link2 link3 …
With each line representing a page, and each item separated by tabs
