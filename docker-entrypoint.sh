#!/bin/bash

# Check if the Firebase service account file exists
if [ ! -f /app/config/serviceAccountKey.json ]; then
    echo "Error: Firebase service account file not found"
    exit 1
fi

ls

# Start the application with external config
exec java -jar \
    -Dspring.config.location=file:/app/config/application.properties \
    -Dserver.address=0.0.0.0 \
    app.jar
