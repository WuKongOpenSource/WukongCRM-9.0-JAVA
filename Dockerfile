FROM openjdk:8-jre
COPY ./target/crm9-release/crm9 /crm9
WORKDIR /
EXPOSE 80
CMD java -Xverify:none -cp /crm9/config:/crm9/lib/* com.kakarote.crm9.Application
