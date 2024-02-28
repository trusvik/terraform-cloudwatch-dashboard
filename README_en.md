# Metrics med Spring Boot og CloudWatch & Terraform


In this exercise, you will become familiar with how to instrument a Spring Boot application with Metrics. 
We will also look at how we can visualize Metrics in AWS CloudWatch, and how we can use Terraform to create a dashboard.

The application in this repository is an "mock" bank application

## We will do this exercise from Cloud 9

Log in to your Cloud 9 environment as usual


## Terraform pro tip 

Instead of using the Terraform installation that comes with Cloud9, we can use "tfenv" - a tool that 
allows us to download and use different versions of Terraform. This is very useful to know since you might work in an 
environment with several different projects or teams that use different Terraform versions.

```sh   
git clone https://github.com/tfutils/tfenv.git ~/.tfenv
echo 'export PATH="$HOME/.tfenv/bin:$PATH"' >> ~/.bash_profile
sudo ln -s ~/.tfenv/bin/* /usr/local/bin
```

To see what versions are available 

```sh
tfenv list-remote
```

Let's pick 1.7.4 which is pretty recent

```sh
tfenv install 1.7.4
```

Set up terraform to use this version 

```sh
tfenv use 1.7.4
terraform --version
```

Check that it worked!

## Install JQ

Jq is a great tool to work with JSON from the command line 

```
sudo yum install jq
```

## Use Terraform to Create a CloudWatch Dashboard

* Clone this repo into your Cloud9 environment.
* Look in the "infra" directory - here you will find the file dashboard.tf which contains Terraform code for a CloudWatch Dashboard.
* As you can see, the dashboard is described in a JSON format. Here you can find documentation https://docs.aws.amazon.com/AmazonCloudWatch/latest/APIReference/CloudWatch-Dashboard-Body-Structure.html
Here you also see how one often includes text or code using "Heredoc" syntax in Terraform code, so that we don't have to worry about "newline", "Escaping" of special characters, etc. (https://developer.hashicorp.com/terraform/language/expressions/strings)

```hcl
resource "aws_cloudwatch_dashboard" "main" {
  dashboard_name = var.student_name
  dashboard_body = <<DEATHSTAR
{
  "widgets": [
    {
      "type": "metric",
      "x": 0,
      "y": 0,
      "width": 12,
      "height": 6,
      "properties": {
        "metrics": [
          [
            "${var.student_name}",
            "account_count.value"
          ]
        ],
        "period": 300,
        "stat": "Maximum",
        "region": "eu-west-1",
        "title": "Total number of accounts"
      }
    }
  ]
}
DEATHSTAR
}
```
## Task 

* Run terraform plan / apply from your Cloud 9 environment See that a Dashboard is created in CloudWatch

## Look at the Spring Boot application

Oepn *BankAccountController.Java* , You'll find this code

```java
    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        Gauge.builder("account_count", theBank,
                b -> b.values().size()).register(meterRegistry);
    }
```

This creates a new metric - of the type Gauge, which constantly reports how many bank accounts exist in the system by 
counting the size of the values in the map holding the accounts. 

## Modify the MetricsConfig Class

You need to modify the MetricsConfig class and use your own name instead of glennbech in the code block

````java
 return new CloudWatchConfig() {
        private Map<String, String> configuration = Map.of(
                "cloudwatch.namespace", "glennbech",
                "cloudwatch.step", Duration.ofSeconds(5).toString());
        
        ....
    };
````

Install Maven in Cloud 9. We will try to run the Spring Boot application from Maven in the terminal.

```
sudo wget http://repos.fedorapeople.org/repos/dchen/apache-maven/epel-apache-maven.repo -O /etc/yum.repos.d/epel-apache-maven.repo
sudo sed -i s/\$releasever/6/g /etc/yum.repos.d/epel-apache-maven.repo
sudo yum install -y apache-maven
sudo yum install jq
```

## Start Spring Boot applikasjonen 

Start the Spring boot app with maven 
```
mvn spring-boot:run
```

The code in this repository exposes a REST interface at http://localhost:8080/account

## Use curl to test the API from cloud 9

curl is a command-line tool used to transfer data to or from a server, supporting a wide range of protocols 
including HTTP, HTTPS, FTP, and more. It is widely used for testing, sending 
requests, and interacting with APIs directly from the terminal or in scripts.

### Operations 

Create an account with an id and balance 

```sh
curl --location --request POST 'http://localhost:8080/account' \
--header 'Content-Type: application/json' \
--data-raw '{
    "id": 1,
    "balance" : "100000"
}'|jq
```

* See information about an account 
* 
```sh 
  curl --location --request GET 'http://localhost:8080/account/1' \
  --header 'Content-Type: application/json'|jq
```

* Transfer money from one account to another 

```sh
curl --location --request POST 'http://localhost:8080/account/2/transfer/3' \
--header 'Content-Type: application/json' \
--data-raw '{
    "fromCountry": "SE",
    "toCountry" : "US",
    "amount" : 500
}
'|jq
```

## Check that we see data in the dashboard

* Go to the AWS UI, and select the CloudWatch service. Choose "Dashboards".
* Search for your own student name and open the dashboard you created.
* See that you get data points on the graph.

It should look something like this:

![Alt text](img/dashboard.png  "a title")

## Oppgaver

* Add more Metrics to your code and dashboard.
* Can you create a new endpoint with new functionality?
* Can you create a Gauge that returns the total amount of money in the bank?
* Feel free to use the following guide for inspiration https://www.baeldung.com/micrometer
* Reference implementation; https://micrometer.io/docs/concepts
* Useful links; 

- https://spring.io/blog/2018/03/16/micrometer-spring-boot-2-s-new-application-metrics-collector
- https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#production-ready-metrics
