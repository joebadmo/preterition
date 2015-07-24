FROM java:8
MAINTAINER Joe Moon <joe@xoxomoon.com>

EXPOSE 3000
ADD ./preterition.jar ./preterition.jar
CMD ["sh", "-c", "java -Ddatabase.url=//db/preterition -Ddatabase.user=preterition -Ddatabase.password=${DB_ENV_POSTGRES_PASSWORD} -jar -Xmx512M ./preterition.jar"]
