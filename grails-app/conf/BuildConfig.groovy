grails.project.work.dir = "../../work/RoomPlanner"

grails.servlet.version = "3.0" // Change depending on target container compliance (2.5 or 3.0)
grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
grails.project.target.level = 1.6
grails.project.source.level = 1.6
//grails.project.war.file = "target/${appName}-${appVersion}.war"

grails.project.fork = [
    // configure settings for compilation JVM, note that if you alter the Groovy version forked compilation is required
    //  compile: [maxMemory: 256, minMemory: 64, debug: false, maxPerm: 256, daemon:true],

    // configure settings for the test-app JVM, uses the daemon by default
    test: false, //[maxMemory: 768, minMemory: 64, debug: false, maxPerm: 256, daemon:true],
    // configure settings for the run-app JVM
    run: false, //[maxMemory: 768, minMemory: 64, debug: false, maxPerm: 256, forkReserve:false],
    // configure settings for the run-war JVM
    war: false, //[maxMemory: 768, minMemory: 64, debug: false, maxPerm: 256, forkReserve:false],
    // configure settings for the Console UI JVM
    console: [maxMemory: 768, minMemory: 64, debug: false, maxPerm: 256]
]

def env = System.getProperty('grails.env')

if (env in ['development', 'test']) {
    grails.server.port.http = 8080
}
else {
    grails.server.port.http = 80
}

// Code snapshot to access config variables
// def directory = new File(getClass().protectionDomain.codeSource.location.path).parent
// def config = new ConfigSlurper(grailsSettings.grailsEnv).parse(new File(directory + File.separator + "Config.groovy").toURI().toURL())
// println config.bootstrapPath

/**
    Define versions
*/
def mysqlConnectorVersion = '5.1.28'
def optaplannerVersion = '6.0.1.Final'
def hibernateVersion = '4.1.11.4'
def roomplannerApiVersion = '0.5-SNAPSHOT'
def roombixUiVersion = '0.1-SNAPSHOT'

/**
    Set configuration values
*/
roomplanner {
    mysql {
        connector.version = mysqlConnectorVersion
    }
    optaplanner {
        version = optaplannerVersion
    }
    hibernate {
        version = hibernateVersion
    }
    roomplannerApi {
        version = roomplannerApiVersion
    }
    roombixUi {
        version = roombixUiVersion
    }
}

grails.project.dependency.resolver = "maven" // or ivy
grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits("global") {
        // specify dependency exclusions here; for example, uncomment this to disable ehcache:
        // excludes 'ehcache'
    }
    log "error" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
    checksums true // Whether to verify checksums on resolve

    repositories {
        inherits true // Whether to inherit repository definitions from plugins

        grailsPlugins()
        grailsHome()
        mavenLocal()
        grailsCentral()
        mavenCentral()

        // uncomment these (or add new ones) to enable remote dependency resolution from public Maven repositories
        //mavenRepo "http://snapshots.repository.codehaus.org"
        //mavenRepo "http://repository.codehaus.org"
        //mavenRepo "http://download.java.net/maven/2/"
        //mavenRepo "http://repository.jboss.com/maven2/"

        mavenRepo 'http://192.168.0.37:8080/artifactory/HMS'
    }

    dependencies {
        // specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes eg.
        runtime "mysql:mysql-connector-java:$mysqlConnectorVersion"

        compile "org.optaplanner:optaplanner-core:$optaplannerVersion"
        compile "joda-time:joda-time:2.3"
        //compile 'joda-time:joda-time-hibernate:1.3'

        // WSS4J
        compile 'org.apache.ws.security:wss4j:1.6.12'
        compile 'org.apache.cxf:cxf-rt-ws-security:2.7.7'

        test "org.spockframework:spock-grails-support:0.7-groovy-2.0"
    }

    plugins {
        compile ":cxf:1.1.1"
        compile ":remoting:1.3"

        compile ":quartz2:2.1.6.2"
        
        //compile ":asset-pipeline:1.0.4"
        compile ":less-asset-pipeline:1.2.0"

        runtime ":database-migration:1.3.8"

        //runtime ":hibernate:3.6.10.1" 
        runtime ":hibernate4:$hibernateVersion"

        // Uncomment these (or add new ones) to enable additional resources capabilities
        //runtime ":zipped-resources:1.0"
        //runtime ":cached-resources:1.0"
        //runtime ":yui-minify-resources:0.1.4"

        build ":tomcat:7.0.47"

        test (":spock:0.7") {
            exclude "spock-grails-support"
            export = false
        }   

        test (":codenarc:0.20") {
            export = false
        }

        if (env == 'jenkins') {
            test (":code-coverage:1.2.6") {
                export = false
            }
        }
        //test ":build-test-data:2.0.5"

        runtime ":roomplanner-api:$roomplannerApiVersion"
        runtime ":roombix-ui:$roombixUiVersion"
    }
}

codenarc {
    extraIncludeDirs = ['grails-app/jobs']
    reports = {
        Jenkins('xml') {                    
            outputFile = 'target/analysis-reports/CodeNarcReport.xml'
            title = 'CodeNarc Analysis Report'
        }
    }
}

