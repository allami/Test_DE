<center><font size="3"> Clustering using Spark MLLIB (hierarchical clustering):</font></center>
 <font size="3"> Packaging Application :</font>

Execute sbt package to package the application

The application uses  external classes and the ones that  comes with Spark .


The application is going to be deployed to local[*]. Change it to whatever cluster you want in the configuration file (reference.conf)


/spark/bin/spark-submit --master "local[*]" --class fr.allami.test.clustering.Job /target/scala-2.11/test_de_2.11-0.1.jar  [inputFile] [outputFolder]

[inputFile]   the path of input data , it's  optional,  if not specified the program will use the file under resources

[outputFolder]  by default it's /tmp/output , it's optional


