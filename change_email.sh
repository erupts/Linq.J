#!/bin/bash
OLD_EMAIL="yuepeng.li@ideamake.cn" # 原始邮箱地址
CORRECTED_NAME="YuePeng"   # 正确的姓名
CORRECTED_EMAIL="erupts@126.com" # 正确的邮箱地址

if [ "$GIT_COMMITTER_EMAIL" = "$OLD_EMAIL" ]
then
    export GIT_AUTHOR_NAME="$CORRECTED_NAME"
    export GIT_AUTHOR_EMAIL="$CORRECTED_EMAIL"
fi
if [ "$GIT_AUTHOR_EMAIL" = "$OLD_EMAIL" ]
then
    export GIT_AUTHOR_NAME="$CORRECTED_NAME"
    export GIT_AUTHOR_EMAIL="$CORRECTED_EMAIL"
fi