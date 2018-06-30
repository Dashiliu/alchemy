// import { Component, OnInit } from '@angular/core';
// import { ActivatedRoute, Router } from '@angular/router';
// import { ConfService } from './conf.service';
// import { Conf } from '../model/conf.model';
// import {Content} from "../model/content.model";
// import {KafkaSource} from "../model/kafka.source.model";
// import {Udf} from "../model/udf.model";
// import {EsSink} from "../model/es.sink.model";
// import {HbaseSink} from "../model/hbase.sink.model";
// import {RedisSink} from "../model/redis.sink.model";
// import {TsdbSink} from "../model/tsdb.sink.model";
// import {Sql} from "../model/sql.model";
// import {Field} from "../model/field.model";
// import 'codemirror/mode/yaml/yaml';
// import 'codemirror/mode/groovy/groovy'
//
// @Component({
//     selector: 'jhi-conf-update',
//     templateUrl: 'conf-update.component.html'
// })
// export class ConfUpdateComponent implements OnInit {
//     selectedDescriptor: any;
//     conf: Conf;
//     descriptors: any[];
//     property: any;
//     field: Field=new Field;
//     isSaving: boolean;
//     code: string;
//     config: any;
//     content: any;
//
//
//     /*
//      JSON.parse(jsonstr); //可以将json字符串转换成json对象
//
//      JSON.stringify(jsonobj); //可以将json对象转换成json对符串
//
//
//      */
//     constructor(private confService: ConfService, private route: ActivatedRoute, private router: Router) {
//
//         this.route.data.subscribe(({ conf }) => {
//             this.conf = conf.body ? conf.body : conf;
//             if(this.conf.content){
//                 this.content=JSON.parse(this.conf.content);
//             }else{
//                 this.content=new Content;
//             }
//             if(!this.conf.id){
//                 this.loadDescriptor();
//             }
//
//         });
//         this.config = { lineNumbers: true, mode: 'yaml' };
//         this.content = `tables:
//   - name: TaxiRides
//     type: source
//     schema:
//       - name: rideId
//         type: LONG
//       - name: lon
//         type: FLOAT
//       - name: lat
//         type: FLOAT
//       - name: rowTime
//         type: TIMESTAMP
//         rowtime:
//           timestamps:
//             type: "from-field"
//             from: "rideTime"
//           watermarks:
//             type: "periodic-bounded"
//             delay: "60000"
//       - name: procTime
//         type: TIMESTAMP
//         proctime: true
//     connector:
//       property-version: 1
//       type: kafka
//       version: 0.11
//       topic: TaxiRides
//       startup-mode: earliest-offset
//       properties:
//         - key: zookeeper.connect
//           value: localhost:2181
//         - key: bootstrap.servers
//           value: localhost:9092
//         - key: group.id
//           value: testGroup
//     format:
//       property-version: 1
//       type: json
//       schema: "ROW(rideId LONG, lon FLOAT, lat FLOAT, rideTime TIMESTAMP)"`;
//
//     }
//
//     ngOnInit() {
//         this.property={key:"",value:""};
//         this.isSaving = false;
//     }
//
//     previousState() {
//         this.router.navigate(['/confs/:jobId/info', this.conf.acJobId]);
//     }
//
//     save() {
//         this.isSaving = true;
//         if (this.conf.id !== null) {
//             this.confService.update(this.conf).subscribe(response => this.onSaveSuccess(response), () => this.onSaveError());
//         } else {
//             this.confService.create(this.conf).subscribe(response => this.onSaveSuccess(response), () => this.onSaveError());
//         }
//     }
//
//     private onSaveSuccess(result) {
//         this.isSaving = false;
//         this.previousState();
//     }
//
//     private onSaveError() {
//         this.isSaving = false;
//     }
//
//     private loadDescriptor() {
//         const type=this.conf.type;
//         if(type=="1"){
//             this.descriptors=[
//                 {label:"kafka",value:"kafkaSource"}
//             ]
//         }else if(type=="2"){
//             this.content.contentType="udf";
//         }else if(type=="3"){
//             this.descriptors=[
//                 {label:"elasticsearch",value:"esSink"},
//                 {label:"Hbase",value:"hbaseSink"},
//                 {label:"Redis",value:"redisSink"},
//                 {label:"Opentsdb",value:"tsdbSink"},
//             ]
//         }
//     }
//
//     selectDescriptor(event) {
//         if(event=="kafkaSource"){
//             this.content.descriptor=new KafkaSource;
//         }else if(event=="udf"){
//             this.content.descriptor=new Udf;
//         }else if(event=="esSink"){
//             this.content.descriptor=new EsSink;
//         }else if(event=="hbaseSink"){
//             this.content.descriptor=new HbaseSink;
//         }else if(event=="redisSink"){
//             this.content.descriptor=new RedisSink;
//         }else if(event=="tsdbSink"){
//             this.content.descriptor=new TsdbSink;
//         } else if(event=="sql"){
//             this.content.descriptor=new Sql;
//         }
//     }
//
//     getKeys(map){
//         return Array.from(map.keys());
//     }
//
//     addProperty(){
//         this.content.descriptor['properties'].set(this.property.key,this.property.value);
//         this.property={key:"",value:""};
//     }
//
//     delProperty(key){
//         this.content.descriptor['properties'].delete(key);
//     }
//
//     addField(){
//         this.content.descriptor['output']['fields'].push(this.field);
//         this.field=new Field;
//     }
// }
