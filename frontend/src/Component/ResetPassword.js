import { useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import axios from "axios";
import LoggedOutNavbar from "../Component/LoggedOutNavbar";
import Swal from "sweetalert2";

export default function ResetPassword() {
    const [newPassword, setNewPassword] = useState("");
    const [confirmPassword, setConfirmPassword] = useState("");
    const [error, setError] = useState("");
    const navigate = useNavigate();
    const location = useLocation();
    const { email } = location.state || {};

    const handleSubmit = async (e) => {
        e.preventDefault();

        if (newPassword !== confirmPassword) {
            setError("Passwords do not match");
            return;
        }

        try {
            await axios.post("http://localhost:8081/user/reset/password", { email, newPassword });
            Swal.fire({
                icon: 'success',
                text: "Password reset successfully",
                customClass: {
                    popup: 'my-swal-popup',
                    title: 'my-swal-title',
                    htmlContainer: 'my-swal-content',
                    confirmButton: 'my-swal-confirm-button',
                }
            });
            navigate("/login");
        } catch (err) {
            const errorMessage = err.response?.data?.error || "Failed to reset password";
            setError(errorMessage);
        }
    };

    return (
        <>
            <LoggedOutNavbar />
            <div className="container mt-20">
                <div className="row justify-content-center">
                    <div className="col-md-6">
                        <div className="card shadow border-0">
                            <div className="card-body">
                                <h4 className="card-title text-center text-123458">Reset Password</h4>
                                <form onSubmit={handleSubmit}>
                                    <div className="mb-3">
                                        <label className="form-label text-123458">New Password</label>
                                        <input type="password" className="form-control" value={newPassword} onChange={(e) => setNewPassword(e.target.value)} required />
                                    </div>
                                    <div className="mb-3">
                                        <label className="form-label text-123458">Confirm Password</label>
                                        <input type="password" className="form-control" value={confirmPassword} onChange={(e) => setConfirmPassword(e.target.value)} required />
                                    </div>
                                    {error && <p className="text-danger text-center fw-bold">{error}</p>}
                                    <button type="submit" className="btn btn-123458 w-100">Reset Password</button>
                                </form>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </>
    );
}
