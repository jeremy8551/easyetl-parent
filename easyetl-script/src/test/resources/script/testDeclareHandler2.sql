
echo ""
echo ""

handler

if `handler | wc -l` != 3 then
  echo 子脚本未继承父脚本中的异常处理逻辑
  exit 1
fi


