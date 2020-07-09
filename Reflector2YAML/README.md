# Reflector2YAML


This project ports a C# (CSharp) cs data model (all classes from any given assembly), to a YAML file.

CSharp => YAML => .java

As this process is reflection based, it's automated, and thus minimizes the chance for typos and bugs in the process of porting of the code from one framework to another.

* This solution:

The intention is to take a cs data model (schema) (CS classes) that are used as a data model for serializing JSON. 
This tool can work on any assembly. Whether we have the source code for this assembly or not. 
You can add an assembly reference and reflect any external assembly, or any assembly in the solution.
If more than one assembly is required, simply concatinage the YAML files into one unified file.
The data model is analyzed via reflection, analyzing class names, inheritence, JsonProperties. 
Generating YAML representing the cs schema, as the json serializer sees it. 
This cs solution comes with a SampleAssembly. 

* INPUT: Any CS assembly (to be analyzed/reflected).
* OUTPUT: cs2Java/YAMLParser/dataModel.yaml
