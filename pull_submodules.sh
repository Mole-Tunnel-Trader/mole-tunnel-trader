#!/bin/bash

# .gitmodules에서 submodule path 목록을 가져오기
MODULE_PATHS=$(git config -f .gitmodules --get-regexp submodule.*.path | awk '{ print $2 }')

echo "===== Submodule Pull Script 시작 ====="

for MOD in $MODULE_PATHS
do
  echo "[Pulling] 서브모듈: $MOD"
  # 서브모듈 디렉토리로 이동
  cd "$MOD" || exit 1

  # main 브랜치 체크아웃 후 최신 pull
  git checkout main
  git pull origin main

  # 상위 디렉토리(프로젝트 루트)로 복귀
  cd - >/dev/null || exit
done

echo "===== 모든 서브모듈 pull 완료 ====="
