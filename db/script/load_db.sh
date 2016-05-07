curl -XPOST 'http://docker:9200/message/domain' -d @../data/domain.json
curl -XPOST 'http://docker:9200/message/language' -d @../data/language_eng.json
curl -XPOST 'http://docker:9200/message/language' -d @../data/language_fra.json