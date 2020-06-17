README - Distributed Assignment 1


Steps to run the program:

*  From now we will refer to / as the project's directory

# Downloading the libraries:
3. At / run: 	sudo apt-get install curl

curl -O https://jarbucketholderofec2.s3.amazonaws.com/ejml-0.23.jar
curl -O https://jarbucketholderofec2.s3.amazonaws.com/jollyday-0.4.7.jar
curl -O https://jarbucketholderofec2.s3.amazonaws.com/stanford-corenlp-3.3.0.jar
curl -O https://jarbucketholderofec2.s3.amazonaws.com/stanford-corenlp-3.3.0-models.jar

** Change the <systemPath> in the pom.xml for each <dependency> to be /

4. At / run:
		mvn clean compile assembly:single
5. At / run:
		cd target
6. At /target/ run:
		mv dspAss1-1.0-SNAPSHOT-jar-with-dependencies.jar ..
7. At /target/ run:
		cd ..
8. At / run:
		java -jar dspAss1-1.0-SNAPSHOT-jar-with-dependencies.jar <input file name1> <input file nameN> <output file name1> <output file nameN> <n> [terminate]
* you can run the program on more than 1 input file.
** make sure the json files are in the right format

9. When the program has finished running, a file named <output file name>.html will appear at /
   You could run it using Firefox (for example) as:
   At / run:
   		firefox <output file name>.html
   		<Replace firefox with a given browser or text editor>

### System initialization:

We uploaded the manager.jar file, the worker.jar file and the libraries to a S3 bucket.

We created EC2 instances with differentiated tags(manager /worker), and used a different script for each type in order to initialize the machine.

Initializing instance:  update Java, install AWS and download the manager.jar file for manager instance, and download the worker.jar file and the libraries for a worker instance.

We used the image ami-00eb20669e0990cb4 of type T2Small, an image that supports user-data and allows our program to run instantaneously upon instance startup

The time it took our program to finish working on the input files:  , and the n we used: 200

Additional information:

### Did you think for more than 2 minutes about security? Do not send your credentials in plain text!

We used IAM role assignments and security group to allow "default" initialization for the AWS objects without adding our own credentials to the cloud.



### Did you think about scalability? Will your program work properly when 1 million clients connected at the same time? How about 2 million? 1 billion? Scalability is very important aspect of the system, be sure it is scalable! system limitations?

AWS allows unlimited amount of queues of the SQS service, so our manager implementation holds a hash table of clients (local applications) and holds a unique queue for each client, granting the system the ability to handle 1 client or more as needed (There is no limit to the amount of clients it can handle).
In addition, we restricted the number of worker instances the program can activate, 1 worker for each n reviews that the client supply.



### What about persistence? What if a node dies? What if a node stalls for a while? Have you taken care of all possible outcomes in the system? Think of more possible issues that might arise from failures. What did you do to solve it? What about broken communications? Be sure to handle all fail-cases!

If a node(aka worker instance) dies or stalls a message for too long, the Visibility Timeout mechanism handles it.
The way it works is once a message has been taken out of a queue, it's considered "in-flight" and is now invisible for 180 seconds while the instance handles it.
If the instance manages to handle it in time, it removes the message from the queue and can continue to other assignments.
If the instance dies or stalls, the timer kicks in and "returns" the message to the queue so that other workers can pick up the message and complete it.



### Threads in your application, when is it a good idea? When is it bad? Invest time to think about threads in your application!

Adding threads to the application might look effective, due to the fact we are currently concurrently handling assignments.
We implemented 2 runnable classes to help the manager with new tasks and the responds from the workers, helping with communications that takes more time.
In practice, this adds a layer of deep complexity to the worker implementation.


### Did you manage the termination process?

we implement that by sending a new task terminate from the user how wrote that to the manager. The manager stop receiving new input files from new users, but do serve the user who send the termination message.
The manager wait for all the workers to finish their job, terminate them and only then terminate itself.


### Are all your workers working hard? Or some are slacking? Why?

As the system is defined, we might have workers doing most of the work and other workers doing very small amount of work, due to the fact we are not holding a balance factor of some sort,
we could also let the manager deal out messages instead of each worker autonomously taking a message.



