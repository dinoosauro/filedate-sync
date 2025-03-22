# FileDate Sync

Sync the "Last modified" metadata of multiple files on your phone with the
original one on other devices.

For example, let's suppose you've copied lots of file from your computer to your
phone. You surely have the original "Last modified" field on your computer, but
on your phone you might have the time the files have been copied. With this app,
you can edit that field by taking the "Last modified" metadata of the files from
your computer.

## Generate a file

To do so, you'll need to generate a file that contains the file directory and
the last modified date. Don't worry, this can be done automatically.

The easiest method is to click
[here](https://dinoosauro.github.io/filedate-sync/Web.html), choose the files or
the folder that contains the files you need to copy the "Last modified" date of,
and copy the file to your phone.

You can also find on the [CreateFile](./CreateFile/) folder both a
[simple webpage](./CreateFile/Web.html) and a
[Node.JS script](./CreateFile/NodeJS.cjs) to create this file. Otherwise, you
can create the file directly from the application in the "Generate" section.

## Apply edits

To apply edits, open FileDate Sync. First, choose the folder by writing its path
(or by opening the folder picker by clicking the button at the right); and later
choose the file generated before.

Note: due to Android's restrictions, starting from Android 10 you'll need to
grant full system storage access.

## App screenshot

Below you can find a screenshot of the UI of the "Apply" section. The UI is
really similar also in all other sections.

![Main App UI](./Screenshot_20250322_185431_FileDate%20Sync.jpeg)
