FROM python:3.6

ARG CACHE_DATE=not_a_date

RUN git clone https://github.com/behrouzsh/deepPhosAPI.git
RUN git clone https://github.com/uc-bd2k/pinet-server.git

RUN pip3 install virtualenv
RUN apt-get update && apt-get install -y gradle

RUN cd /pinet-server && gradle clean build -x test

RUN cd /deepPhosAPI && virtualenv -p python3 env
RUN cd /deepPhosAPI && \
   . env/bin/activate && \
   pip3 install -r requirements.txt && \
   pip3 install sklearn

COPY run.sh .
RUN chmod a+x run.sh
EXPOSE 5000
EXPOSE 8090
CMD ./run.sh
