ARG CACHE_DATE=not_a_date


RUN git clone https://github.com/uc-bd2k/pinet-server.git

RUN apt-get update && apt-get install -y gradle

RUN cd /pinet-server && gradle clean build -x test

COPY run.sh .
RUN chmod a+x run.sh
EXPOSE 8090
CMD ./run.sh
