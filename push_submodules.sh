#!/bin/bash

# .gitmodules에서 submodule path 목록 가져오기
MODULE_PATHS=$(git config -f .gitmodules --get-regexp submodule.*.path | awk '{ print $2 }')

# 커밋 메시지를 인자로 받되, 없으면 기본 메시지 사용
COMMIT_MSG=${1:-"Update submodule"}

echo "===== Submodule Push Script 시작 ====="

for MOD in $MODULE_PATHS
do
  echo "[Pushing] 서브모듈: $MOD"
  cd "$MOD" || exit 1

  # main 브랜치 이동
  git checkout main

  # 변경사항 스테이징 & 커밋
  git add .
  if ! git diff --cached --quiet; then
    git commit -m "$COMMIT_MSG"
    git push origin main
  else
    echo "  변경사항 없음"
  fi

  cd - >/dev/null || exit
done
echo "===== Submodule push 완료 ====="
