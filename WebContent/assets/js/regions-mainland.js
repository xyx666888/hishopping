// Mainland China region data for address selectors.
// Keep this file UTF-8 encoded; external scripts reference it with charset="UTF-8".
var MAINLAND_REGION_DATA = [
  { name: "北京市", code: 110000, cities: [ { name: "北京市", code: 110100, districts: [
    { name: "东城区", code: 110101 }, { name: "西城区", code: 110102 }, { name: "朝阳区", code: 110105 },
    { name: "海淀区", code: 110108 }, { name: "丰台区", code: 110106 }, { name: "通州区", code: 110112 }
  ] } ] },
  { name: "天津市", code: 120000, cities: [ { name: "天津市", code: 120100, districts: [
    { name: "和平区", code: 120101 }, { name: "河东区", code: 120102 }, { name: "南开区", code: 120104 },
    { name: "河西区", code: 120103 }, { name: "滨海新区", code: 120116 }
  ] } ] },
  { name: "河北省", code: 130000, cities: [
    { name: "石家庄市", code: 130100, districts: [ { name: "长安区", code: 130102 }, { name: "桥西区", code: 130104 }, { name: "裕华区", code: 130108 } ] },
    { name: "唐山市", code: 130200, districts: [ { name: "路南区", code: 130202 }, { name: "路北区", code: 130203 } ] }
  ] },
  { name: "山西省", code: 140000, cities: [ { name: "太原市", code: 140100, districts: [ { name: "小店区", code: 140105 }, { name: "迎泽区", code: 140106 }, { name: "杏花岭区", code: 140107 } ] } ] },
  { name: "内蒙古自治区", code: 150000, cities: [ { name: "呼和浩特市", code: 150100, districts: [ { name: "新城区", code: 150102 }, { name: "回民区", code: 150103 }, { name: "赛罕区", code: 150105 } ] } ] },
  { name: "辽宁省", code: 210000, cities: [ { name: "沈阳市", code: 210100, districts: [ { name: "和平区", code: 210102 }, { name: "沈河区", code: 210103 }, { name: "铁西区", code: 210106 } ] } ] },
  { name: "吉林省", code: 220000, cities: [ { name: "长春市", code: 220100, districts: [ { name: "南关区", code: 220102 }, { name: "朝阳区", code: 220104 }, { name: "绿园区", code: 220106 } ] } ] },
  { name: "黑龙江省", code: 230000, cities: [ { name: "哈尔滨市", code: 230100, districts: [ { name: "道里区", code: 230102 }, { name: "南岗区", code: 230103 }, { name: "香坊区", code: 230110 } ] } ] },
  { name: "上海市", code: 310000, cities: [ { name: "上海市", code: 310100, districts: [
    { name: "黄浦区", code: 310101 }, { name: "徐汇区", code: 310104 }, { name: "静安区", code: 310106 },
    { name: "浦东新区", code: 310115 }, { name: "闵行区", code: 310112 }
  ] } ] },
  { name: "江苏省", code: 320000, cities: [
    { name: "南京市", code: 320100, districts: [ { name: "玄武区", code: 320102 }, { name: "秦淮区", code: 320104 }, { name: "鼓楼区", code: 320106 }, { name: "江宁区", code: 320115 } ] },
    { name: "苏州市", code: 320500, districts: [ { name: "姑苏区", code: 320508 }, { name: "吴中区", code: 320506 }, { name: "工业园区", code: 320571 } ] }
  ] },
  { name: "浙江省", code: 330000, cities: [ { name: "杭州市", code: 330100, districts: [ { name: "上城区", code: 330102 }, { name: "西湖区", code: 330106 }, { name: "滨江区", code: 330108 } ] } ] },
  { name: "安徽省", code: 340000, cities: [ { name: "合肥市", code: 340100, districts: [ { name: "瑶海区", code: 340102 }, { name: "庐阳区", code: 340103 }, { name: "蜀山区", code: 340104 } ] } ] },
  { name: "福建省", code: 350000, cities: [ { name: "福州市", code: 350100, districts: [ { name: "鼓楼区", code: 350102 }, { name: "台江区", code: 350103 }, { name: "仓山区", code: 350104 } ] } ] },
  { name: "江西省", code: 360000, cities: [ { name: "南昌市", code: 360100, districts: [ { name: "东湖区", code: 360102 }, { name: "西湖区", code: 360103 }, { name: "青山湖区", code: 360111 } ] } ] },
  { name: "山东省", code: 370000, cities: [ { name: "济南市", code: 370100, districts: [ { name: "历下区", code: 370102 }, { name: "市中区", code: 370103 }, { name: "槐荫区", code: 370104 } ] } ] },
  { name: "河南省", code: 410000, cities: [ { name: "郑州市", code: 410100, districts: [ { name: "中原区", code: 410102 }, { name: "二七区", code: 410103 }, { name: "金水区", code: 410105 } ] } ] },
  { name: "湖北省", code: 420000, cities: [ { name: "武汉市", code: 420100, districts: [ { name: "江岸区", code: 420102 }, { name: "武昌区", code: 420106 }, { name: "洪山区", code: 420111 } ] } ] },
  { name: "湖南省", code: 430000, cities: [ { name: "长沙市", code: 430100, districts: [ { name: "芙蓉区", code: 430102 }, { name: "天心区", code: 430103 }, { name: "岳麓区", code: 430104 } ] } ] },
  { name: "广东省", code: 440000, cities: [
    { name: "广州市", code: 440100, districts: [ { name: "越秀区", code: 440104 }, { name: "天河区", code: 440106 }, { name: "番禺区", code: 440113 } ] },
    { name: "深圳市", code: 440300, districts: [ { name: "福田区", code: 440304 }, { name: "南山区", code: 440305 }, { name: "宝安区", code: 440306 } ] }
  ] },
  { name: "广西壮族自治区", code: 450000, cities: [ { name: "南宁市", code: 450100, districts: [ { name: "兴宁区", code: 450102 }, { name: "青秀区", code: 450103 }, { name: "江南区", code: 450105 } ] } ] },
  { name: "海南省", code: 460000, cities: [ { name: "海口市", code: 460100, districts: [ { name: "秀英区", code: 460105 }, { name: "龙华区", code: 460106 }, { name: "美兰区", code: 460108 } ] } ] },
  { name: "重庆市", code: 500000, cities: [ { name: "重庆市", code: 500100, districts: [ { name: "渝中区", code: 500103 }, { name: "江北区", code: 500105 }, { name: "沙坪坝区", code: 500106 }, { name: "渝北区", code: 500112 } ] } ] },
  { name: "四川省", code: 510000, cities: [ { name: "成都市", code: 510100, districts: [ { name: "锦江区", code: 510104 }, { name: "青羊区", code: 510105 }, { name: "武侯区", code: 510107 }, { name: "高新区", code: 510191 } ] } ] },
  { name: "贵州省", code: 520000, cities: [ { name: "贵阳市", code: 520100, districts: [ { name: "南明区", code: 520102 }, { name: "云岩区", code: 520103 }, { name: "观山湖区", code: 520115 } ] } ] },
  { name: "云南省", code: 530000, cities: [ { name: "昆明市", code: 530100, districts: [ { name: "五华区", code: 530102 }, { name: "盘龙区", code: 530103 }, { name: "官渡区", code: 530111 } ] } ] },
  { name: "西藏自治区", code: 540000, cities: [ { name: "拉萨市", code: 540100, districts: [ { name: "城关区", code: 540102 }, { name: "堆龙德庆区", code: 540103 } ] } ] },
  { name: "陕西省", code: 610000, cities: [ { name: "西安市", code: 610100, districts: [ { name: "新城区", code: 610102 }, { name: "碑林区", code: 610103 }, { name: "雁塔区", code: 610113 } ] } ] },
  { name: "甘肃省", code: 620000, cities: [ { name: "兰州市", code: 620100, districts: [ { name: "城关区", code: 620102 }, { name: "七里河区", code: 620103 }, { name: "安宁区", code: 620105 } ] } ] },
  { name: "青海省", code: 630000, cities: [ { name: "西宁市", code: 630100, districts: [ { name: "城东区", code: 630102 }, { name: "城中区", code: 630103 }, { name: "城西区", code: 630104 } ] } ] },
  { name: "宁夏回族自治区", code: 640000, cities: [ { name: "银川市", code: 640100, districts: [ { name: "兴庆区", code: 640104 }, { name: "西夏区", code: 640105 }, { name: "金凤区", code: 640106 } ] } ] },
  { name: "新疆维吾尔自治区", code: 650000, cities: [ { name: "乌鲁木齐市", code: 650100, districts: [ { name: "天山区", code: 650102 }, { name: "沙依巴克区", code: 650103 }, { name: "新市区", code: 650104 } ] } ] }
];