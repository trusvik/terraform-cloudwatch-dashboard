# Metrics med Spring Boot og CloudWatch & Terraform

I denne øvingen skal dere bli kjent med hvordan man instrumenterer en Spring Boot applikasjon med Metrics. 
Vi skal også se på hvordan vi kan visualisere Metrics i AWS CloudWatch, og hvordan vi kan bruke terraform til å lage 
et dashboard.

Applikasjonen som ligger i dette repositoriet er en eksempel bank-applikasjon som, ser hør- og bør, er veldig ustabil 
(med vilje). 


## Vi skal gjøre denne øvingen fra Cloud 9 

Logg på Cloud 9 miljøet ditt som vanlig 

## Terraform pro tip 

Istedet for å bruke terraform installasjonen som kommer med Cloud9, kan vi bruke "tfenv" - et verktøy som lar oss laste ned 
og bruke ulike Terraform versjoner. Dette er veldig nyttig å kunne siden dere kanskje skal jobbe i et miljø med flere ulike 
prosjekter- eller team som bruker ulike Terrafsorm versjoner. 

```sh   
git clone https://github.com/tfutils/tfenv.git ~/.tfenv
echo 'export PATH="$HOME/.tfenv/bin:$PATH"' >> ~/.bash_profile
sudo ln -s ~/.tfenv/bin/* /usr/local/bin
```

For å se hvilke Terraform versjoner i kan velge fra -  

```sh
tfenv list-remote
```

```sh
tfenv install 1.3.3
```

Vi ser at terraform 1.3.3 er lastet ned for oss. Vi kan så gjøre 

```sh
tfenv use 1.3.3
terraform --version
```


## Installer JQ

jq er et fint verktøy for å vise JSON på en mer brukervennlig måte 

```
sudo yum install jq
```

## Bruk Terraform til å lage et CloudWatch DashBoard 

* Klon dette repoet til Cloud9 miljøet ditt. 

Se i katalogen "infra" - her finner dere filen *dashboard.tf* som inneholder Terraformkode for et CloudWatch Dashboard.

* Som dere ser beskrives dashboardet i et JSON-format. Her finner dere dokumentasjon https://docs.aws.amazon.com/AmazonCloudWatch/latest/APIReference/CloudWatch-Dashboard-Body-Structure.html
* Her ser dere også hvordan man ofte inkluderer tekst eller kode ved hjelp av  "Heredoc" syntaks i Terraformkode, slik at vi ikke trenger å tenke på "newline", "Escaping" av spesialtegn osv (https://developer.hashicorp.com/terraform/language/expressions/strings)

```hcl
resource "aws_cloudwatch_dashboard" "main" {
  dashboard_name = var.student_name
  dashboard_body = <<DASHBOARD
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
DASHBOARD
}
```
## TODO 

Skriv en *provider.tf* i samme katalog som dashboard.tf - og kjør terraform plan / apply fra Cloud 9 miljøet ditt
Se at Det blir opprettet et Dashboard

* Kjør Terraform  init / plan / apply from Cloud9-miljøet ditt

## Se på Spring Boot appen 

Åpne *BankAccountController.Java* , Her finner dere koden

```java
    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        Gauge.builder("account_count", theBank,
                b -> b.values().size()).register(meterRegistry);
    }
```
Denne lager en Ny metric - av typen Gauge, som hele tiden rapporterer hvor mange bank-kontoer som eksisterer i systemet 

## Endre MetricConfig klasse

Du må endre på klassen *MetricsConfig* og bruke ditt egent studentnavn istedet for *glennbech* i kodeblokken 

````java
 return new CloudWatchConfig() {
        private Map<String, String> configuration = Map.of(
                "cloudwatch.namespace", "glennbech",
                "cloudwatch.step", Duration.ofSeconds(5).toString());
        
        ....
    };
````

Installer maven i Cloud 9. Vi skal forsøke å kjøre Spring Boot applikasjonen fra Maven i terminalen

```
sudo wget http://repos.fedorapeople.org/repos/dchen/apache-maven/epel-apache-maven.repo -O /etc/yum.repos.d/epel-apache-maven.repo
sudo sed -i s/\$releasever/6/g /etc/yum.repos.d/epel-apache-maven.repo
sudo yum install -y apache-maven
sudo yum install jq
```

## Start Spring Boot applikasjonen 

Start applikasjonen med Cloud 9'
```
mvn spring-boot:run
```

Koden i dette repoet eksponerer et REST grensesnitt på http://localhost:8080/account

## Kall APIet fra en terminal I Cloud 9 

* Opprette konto, eller dette saldo

```sh
curl --location --request POST 'http://localhost:8080/account' \
--header 'Content-Type: application/json' \
--data-raw '{
    "id": 1,
    "balance" : "100000"
}'|jq
```

* Se info om en konto
```sh 
  curl --location --request GET 'http://localhost:8080/account/1' \
  --header 'Content-Type: application/json'|jq
```

* Overføre penger fra en konto til en annen

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

## Sjekk at det kommer data i CloudWatch- Dashbordet 

Det skal se omtrent slik ut 

* Går til AWS UI, og tjenesten CloudWatch. Velg "Dashboards".
* Søk på ditt eget studentnavn og åpne dashboardet du lagde
* Se at du får målepunkter på grafen

![Alt text](img/dashboard.png  "a title")


## Oppgaver

* Legg til fler Metrics i koden og Dashboardet ditt
* Kan du lage et nytt endepunkt med ny funksjonalitet? 
* Kan du lage en Gauge som returnerer hvor mye penger som totalt er i banken?
* Kan du lage en GitHub Actions pipeline for terraformkoden / Javakoden? 
* Bruk gjerne følgende guide som inspirasjon https://www.baeldung.com/micrometer
* Referanseimplementasjon; https://micrometer.io/docs/concepts

Nyttig informasjon; 

- https://spring.io/blog/2018/03/16/micrometer-spring-boot-2-s-new-application-metrics-collector
- https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#production-ready-metrics
