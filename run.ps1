# ============================================================================
# Project-TECH - Main Launcher
# ============================================================================

Write-Host ""
Write-Host "=========================================================" -ForegroundColor Cyan
Write-Host "   PROJECT-TECH" -ForegroundColor Cyan
Write-Host "   Multi-Agent Systems & AI Planning" -ForegroundColor Cyan
Write-Host "=========================================================" -ForegroundColor Cyan
Write-Host ""

$ProjectRoot = $PSScriptRoot

# Display menu
Write-Host "Select which part to run:" -ForegroundColor Yellow
Write-Host ""
Write-Host "  [1] Partie-1: English Auction System (JADE)" -ForegroundColor White
Write-Host "  [2] Partie-2: Multi-Criteria Supplier Selection (JADE)" -ForegroundColor White
Write-Host "  [3] Partie-3: AI Planning (Jupyter Notebooks)" -ForegroundColor White
Write-Host "  [Q] Quit" -ForegroundColor Gray
Write-Host ""

$choice = Read-Host "Enter your choice"

switch ($choice) {
    "1" {
        Write-Host ""
        Write-Host "======================================================" -ForegroundColor Cyan
        Write-Host "  PARTIE-1: English Auction System" -ForegroundColor Cyan
        Write-Host "======================================================" -ForegroundColor Cyan
        Write-Host ""
        
        Set-Location "$ProjectRoot\Partie-1"
        
        Write-Host "Building project..." -ForegroundColor Yellow
        mvn clean package -q
        
        if ($LASTEXITCODE -ne 0) {
            Write-Host "Build failed!" -ForegroundColor Red
            exit 1
        }
        
        $jar = Get-ChildItem -Path "target" -Filter "*-jar-with-dependencies.jar" | Select-Object -First 1
        Write-Host "Starting auction system..." -ForegroundColor Green
        Write-Host ""
        
        java -jar "target\$($jar.Name)"
    }
    "2" {
        Write-Host ""
        Write-Host "======================================================" -ForegroundColor Cyan
        Write-Host "  PARTIE-2: Multi-Criteria Supplier Selection" -ForegroundColor Cyan
        Write-Host "======================================================" -ForegroundColor Cyan
        Write-Host ""
        
        Set-Location "$ProjectRoot\Partie-2"
        
        Write-Host "Building project..." -ForegroundColor Yellow
        mvn clean package -q
        
        if ($LASTEXITCODE -ne 0) {
            Write-Host "Build failed!" -ForegroundColor Red
            exit 1
        }
        
        $jar = Get-ChildItem -Path "target" -Filter "*-jar-with-dependencies.jar" | Select-Object -First 1
        Write-Host "Starting supplier selection system..." -ForegroundColor Green
        Write-Host ""
        
        java -jar "target\$($jar.Name)"
    }
    "3" {
        Write-Host ""
        Write-Host "======================================================" -ForegroundColor Cyan
        Write-Host "  PARTIE-3: AI Planning Algorithms" -ForegroundColor Cyan
        Write-Host "======================================================" -ForegroundColor Cyan
        Write-Host ""
        
        # Check Python
        Write-Host "Checking Python..." -ForegroundColor Yellow
        try {
            $pythonCheck = python --version 2>&1
            Write-Host "[OK] Python found: $pythonCheck" -ForegroundColor Green
        } catch {
            Write-Host "[ERROR] Python not found! Please install Python 3.8+" -ForegroundColor Red
            Write-Host "  Download from: https://www.python.org/downloads/" -ForegroundColor Gray
            exit 1
        }
        
        # Check Jupyter
        Write-Host "Checking Jupyter..." -ForegroundColor Yellow
        $jupyterCheck = python -m pip show jupyter 2>&1
        
        if ($LASTEXITCODE -ne 0) {
            Write-Host "[WARN] Jupyter not found" -ForegroundColor Yellow
            Write-Host ""
            Write-Host "Installing Jupyter Notebook..." -ForegroundColor Yellow
            python -m pip install --upgrade pip
            python -m pip install jupyter notebook ipywidgets numpy matplotlib
            
            if ($LASTEXITCODE -ne 0) {
                Write-Host "[ERROR] Installation failed!" -ForegroundColor Red
                exit 1
            }
            Write-Host "[OK] Jupyter installed successfully!" -ForegroundColor Green
        } else {
            Write-Host "[OK] Jupyter found" -ForegroundColor Green
        }
        
        Set-Location "$ProjectRoot\Partie -3"
        
        Write-Host ""
        Write-Host "Starting Jupyter Notebook..." -ForegroundColor Green
        Write-Host ""
        Write-Host "Available notebooks:" -ForegroundColor Yellow
        Write-Host "  - planning_partial_order_planner.ipynb" -ForegroundColor White
        Write-Host "  - planning_graphPlan.ipynb" -ForegroundColor White
        Write-Host "  - planning_hierarchical_search.ipynb" -ForegroundColor White
        Write-Host "  - planning_total_order_planner.ipynb" -ForegroundColor White
        Write-Host ""
        
        python -m notebook
    }
    {$_ -in "Q", "q"} {
        Write-Host ""
        Write-Host "Exiting..." -ForegroundColor Gray
        exit 0
    }
    default {
        Write-Host ""
        Write-Host "Invalid choice!" -ForegroundColor Red
        exit 1
    }
}
