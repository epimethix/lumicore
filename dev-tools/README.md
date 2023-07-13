# lumicore :: DevTools

DevTools aids with development. at the time there is an I18N interface for editing and (AI) translating GUI Labels.

Also there is a diagram tool (UML, ERD) but that is still a work in progress... the diagram tool should at some point in time be able to generate code, primarily the database model classes.

it is sometimes required to refresh the IDEs directories for changes made in DevTools to take effect.

## Workflow: I18N

create the base language properties file for example "src/main/resources/myapp-labels_en.properties".

then launch dev-tools, go to "Open Bundle...", select the properties file, select the Labels constants java file, and edit and translate the labels. !! press save before ai translating !!

## Workflow: Diagrams

Go to "File -> New Diagram".

Select file name and diagram type.

Select the Project classes to add if any.

To add an arbitrary class click the toolbar button "Add Entity".

Click "Manage Entities" to add or remove project entities.

"New Entity": WIP

"Relations": NIY