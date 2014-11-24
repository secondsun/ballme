# BallMe

This is a server for an Android app which maintains informations about football game watching parties and sends relevant updates via push technologies.

## Testing

'''
mvn clean install
'''

Will build and test everything.

## Running

Deploy to WildFly, create a datasource named jndi/ballme

You also need to set the following system settings : 
         
 * sportsdata.api.key Sports Data Key
 * cse.key Google Custom Search Engine Key
 * cse.id Google Custom Search Ending Id.


