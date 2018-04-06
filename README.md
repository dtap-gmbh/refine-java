# OpenRefine API Java Client 

This library is a client implementation for the [OpenRefine API](https://github.com/OpenRefine/OpenRefine/wiki/OpenRefine-API).

Supported functions are

* Create project
* Delete project

Currently missing functions are

* Apply operations
* Export rows
* Check status of async processes
* Get all project metadata
* Expression Preview


## Usage

```java
String url = "http://localhost:3333/";
File file = ...;
   
try (RefineClient client = RefineClients.create(url)) {
         RefineProject project = client.createProject("Project name", file);
         // ...
         client.deleteProject(project.getId());
}
```

More examples can be found in `gmbh.dtap.refine.Usage`.

## Credits

Copyright (c) 2018 DTAP GmbH

Please refer to the LICENSE file for details.
